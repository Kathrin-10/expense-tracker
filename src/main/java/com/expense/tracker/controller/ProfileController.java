package com.expense.tracker.controller;

import com.expense.tracker.dto.UserUpdateRequest;
import com.expense.tracker.security.CurrentUserService;
import com.expense.tracker.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ProfileController {
  private final CurrentUserService current;
  private final AuthService service;

  public ProfileController(CurrentUserService current, AuthService service) {
    this.current = current;
    this.service = service;
  }

  @GetMapping("/profile")
  public String profile(Model model) {
    model.addAttribute("user", current.get());
    return "profile";
  }

  @PutMapping("/api/profile")
  @ResponseBody
  public com.expense.tracker.dto.UserResponse update(@Valid @RequestBody UserUpdateRequest r) {
    var u = current.get();
    var updated =
        service.updateOwn(
            u.getId(), new UserUpdateRequest(r.fullName(), r.email(), r.phone(), null));
    var auth = SecurityContextHolder.getContext().getAuthentication();
    SecurityContextHolder.getContext()
        .setAuthentication(
            new UsernamePasswordAuthenticationToken(
                updated.email(), auth.getCredentials(), auth.getAuthorities()));
    return updated;
  }

  @PostMapping("/api/profile/password")
  @ResponseBody
  public java.util.Map<String, String> password(
      @RequestParam String currentPassword, @RequestParam String newPassword) {
    service.changePassword(current.get().getId(), currentPassword, newPassword);
    return java.util.Map.of("message", "Password updated successfully");
  }
}
