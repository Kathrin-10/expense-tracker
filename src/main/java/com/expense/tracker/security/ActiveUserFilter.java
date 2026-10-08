package com.expense.tracker.security;

import com.expense.tracker.repository.AppUserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.core.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ActiveUserFilter extends OncePerRequestFilter {
  private final AppUserRepository repo;

  public ActiveUserFilter(AppUserRepository repo) {
    this.repo = repo;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    Authentication a = SecurityContextHolder.getContext().getAuthentication();
    if (a != null
        && a.isAuthenticated()
        && !a.getName().equals("anonymousUser")
        && a.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals("ROLE_USER"))) {
      var u = repo.findByEmailIgnoreCase(a.getName());
      if (u.isEmpty() || !u.get().isActive()) {
        SecurityContextHolder.clearContext();
        req.getSession().invalidate();
        if (req.getRequestURI().startsWith("/api/")) {
          res.setStatus(401);
          res.setContentType("application/json");
          res.getWriter().write("{\"message\":\"Your account has been deactivated.\"}");
        } else res.sendRedirect("/login?deactivated=true");
        return;
      }
    }
    chain.doFilter(req, res);
  }
}
