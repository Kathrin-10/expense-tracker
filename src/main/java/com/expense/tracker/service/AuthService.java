package com.expense.tracker.service;

import com.expense.tracker.dto.RegisterRequest;
import com.expense.tracker.dto.UserResponse;
import com.expense.tracker.dto.UserUpdateRequest;
import com.expense.tracker.entity.AppUser;
import com.expense.tracker.entity.PasswordResetToken;
import com.expense.tracker.entity.Role;
import com.expense.tracker.exception.DuplicateUserException;
import com.expense.tracker.exception.ResourceNotFoundException;
import com.expense.tracker.repository.AppUserRepository;
import com.expense.tracker.repository.PasswordResetTokenRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Transactional
public class AuthService {
  private final AppUserRepository users;
  private final PasswordResetTokenRepository tokens;
  private final PasswordEncoder encoder;
  private final EmailService emailService;
  private final String adminEmail;
  private final String baseUrl;

  public AuthService(
      AppUserRepository users,
      PasswordResetTokenRepository tokens,
      PasswordEncoder encoder,
      EmailService emailService,
      @Value("${app.admin.email}") String adminEmail,
      @Value("${app.base-url}") String baseUrl) {
    this.users = users;
    this.tokens = tokens;
    this.encoder = encoder;
    this.emailService = emailService;
    this.adminEmail = adminEmail.trim().toLowerCase();
    this.baseUrl = baseUrl.replaceAll("/+$", "");
  }

  public UserResponse register(RegisterRequest request) {
    String email = request.email().trim().toLowerCase();
    if (email.equals(adminEmail))
      throw new DuplicateUserException("That email is reserved for the administrator");
    if (users.existsByEmailIgnoreCase(email))
      throw new DuplicateUserException("An account with this email already exists");
    AppUser u = new AppUser();
    u.setFullName(request.fullName().trim());
    u.setEmail(email);
    u.setPhone(blank(request.phone()));
    u.setPassword(encoder.encode(request.password()));
    u.setRole(Role.USER);
    u.setActive(true);
    AppUser saved = users.save(u);
    return response(saved);
  }

  public UserResponse updateOwn(Long id, UserUpdateRequest request) {
    AppUser u = get(id);
    apply(u, request, false);
    return response(u);
  }

  public UserResponse adminUpdate(Long id, UserUpdateRequest request) {
    AppUser u = get(id);
    apply(u, request, true);
    return response(u);
  }

  public UserResponse find(Long id) {
    return response(get(id));
  }

  public java.util.List<UserResponse> customers() {
    return users.findAllByRoleOrderByFullNameAsc(Role.USER).stream().map(this::response).toList();
  }

  public void changePassword(Long id, String current, String next) {
    AppUser u = get(id);
    if (!encoder.matches(current, u.getPassword()))
      throw new IllegalArgumentException("Current password is incorrect");
    if (next == null || next.length() < 8)
      throw new IllegalArgumentException("New password must contain at least 8 characters");
    u.setPassword(encoder.encode(next));
  }

  public void createResetToken(String email) {
    emailService.ensureConfigured();
    users
        .findByEmailIgnoreCase(email.trim())
        .filter(u -> u.getRole() == Role.USER)
        .ifPresent(
            u -> {
              tokens.deleteAll(
                  tokens.findAll().stream()
                      .filter(t -> t.getUser().getId().equals(u.getId()))
                      .toList());
              PasswordResetToken t =
                  new PasswordResetToken(
                      UUID.randomUUID().toString(), u, LocalDateTime.now().plusMinutes(30));
              tokens.save(t);
              String resetUrl =
                  UriComponentsBuilder.fromUriString(baseUrl)
                      .pathSegment("reset-password")
                      .queryParam("token", t.getToken())
                      .build()
                      .toUriString();
              emailService.sendPasswordReset(u.getEmail(), resetUrl);
            });
  }

  public boolean validToken(String token) {
    return tokens
        .findByToken(token)
        .filter(t -> !t.isUsed() && t.getExpiresAt().isAfter(LocalDateTime.now()))
        .isPresent();
  }

  public void resetPassword(String token, String password) {
    PasswordResetToken t =
        tokens
            .findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset link"));
    if (t.isUsed() || t.getExpiresAt().isBefore(LocalDateTime.now()))
      throw new IllegalArgumentException("Invalid or expired reset link");
    if (password == null || password.length() < 8)
      throw new IllegalArgumentException("Password must contain at least 8 characters");
    t.getUser().setPassword(encoder.encode(password));
    t.setUsed(true);
  }

  private void apply(AppUser u, UserUpdateRequest r, boolean admin) {
    String email = r.email().trim().toLowerCase();
    if (email.equals(adminEmail) && u.getRole() == Role.USER)
      throw new IllegalArgumentException("That email is reserved for the administrator");
    users
        .findByEmailIgnoreCase(email)
        .ifPresent(
            existing -> {
              if (!existing.getId().equals(u.getId()))
                throw new IllegalArgumentException("That email is already in use");
            });
    u.setFullName(r.fullName().trim());
    u.setEmail(email);
    u.setPhone(blank(r.phone()));
    if (admin && r.active() != null) u.setActive(r.active());
  }

  private AppUser get(Long id) {
    return users
        .findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException("Customer with id " + id + " was not found"));
  }

  private String blank(String v) {
    return v == null || v.isBlank() ? null : v.trim();
  }

  private UserResponse response(AppUser u) {
    return new UserResponse(
        u.getId(), u.getFullName(), u.getEmail(), u.getPhone(), u.getRole(), u.isActive());
  }
}
