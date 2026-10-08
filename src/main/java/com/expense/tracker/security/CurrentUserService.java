package com.expense.tracker.security;

import com.expense.tracker.entity.AppUser;
import com.expense.tracker.exception.ResourceNotFoundException;
import com.expense.tracker.repository.AppUserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
  private final AppUserRepository repository;

  public CurrentUserService(AppUserRepository repository) {
    this.repository = repository;
  }

  public AppUser get() {
    Authentication a = SecurityContextHolder.getContext().getAuthentication();
    if (a == null || !a.isAuthenticated() || a instanceof AnonymousAuthenticationToken)
      throw new IllegalStateException("Authentication required");
    return repository
        .findByEmailIgnoreCase(a.getName())
        .orElseThrow(() -> new ResourceNotFoundException("Authenticated user was not found"));
  }
}
