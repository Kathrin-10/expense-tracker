package com.expense.tracker.security;

import com.expense.tracker.entity.Role;
import com.expense.tracker.repository.AppUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;

@Configuration
public class SecurityConfig {

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }

  @Bean
  SecurityContextRepository securityContextRepository() {
    return new HttpSessionSecurityContextRepository();
  }

  @Bean
  UserDetailsService userDetailsService(AppUserRepository repo) {
    return email ->
        repo.findByEmailIgnoreCase(email.trim())
            .map(
                u ->
                    User.withUsername(u.getEmail())
                        .password(u.getPassword())
                        .roles(u.getRole().name())
                        .disabled(!u.isActive())
                        .build())
            .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
  }

  @Bean
  AuthenticationSuccessHandler successHandler(
      AppUserRepository repo, @Value("${app.admin.email}") String configuredAdminEmail) {

    String adminEmail = configuredAdminEmail.trim().toLowerCase();

    return (request, response, authentication) -> {
      var user = repo.findByEmailIgnoreCase(authentication.getName()).orElseThrow();
      boolean adminLogin = "true".equalsIgnoreCase(request.getParameter("adminLogin"));
      boolean isAdmin =
          user.getRole() == Role.ADMIN && user.getEmail().equalsIgnoreCase(adminEmail);

      if (adminLogin) {
        if (!isAdmin) {
          SecurityContextHolder.clearContext();
          request.getSession().invalidate();
          response.sendRedirect("/admin/login?error=role");
          return;
        }
        response.sendRedirect("/admin/customers");
        return;
      }

      // An administrator can NEVER enter through the customer login form.
      if (isAdmin || user.getRole() != Role.USER) {
        SecurityContextHolder.clearContext();
        request.getSession().invalidate();
        response.sendRedirect("/login?error=customer-only");
        return;
      }

      response.sendRedirect("/dashboard");
    };
  }

  @Bean
  AuthenticationFailureHandler failureHandler() {
    return (request, response, exception) -> {
      boolean adminLogin = "true".equalsIgnoreCase(request.getParameter("adminLogin"));
      response.sendRedirect((adminLogin ? "/admin/login" : "/login") + "?error=true");
    };
  }

  /**
   * The UI sends the raw CookieCsrfTokenRepository token in X-XSRF-TOKEN. Resolve that header
   * explicitly so every AJAX POST/PUT/DELETE request uses the same token that Spring issued to the
   * page.
   */
  private static final class HeaderFirstCsrfTokenRequestHandler implements CsrfTokenRequestHandler {
    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        java.util.function.Supplier<CsrfToken> deferredToken) {
      CsrfToken token = deferredToken.get();
      request.setAttribute(token.getParameterName(), token);
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken token) {
      String headerValue = request.getHeader(token.getHeaderName());
      if (headerValue != null && !headerValue.isBlank()) {
        return headerValue;
      }
      return request.getParameter(token.getParameterName());
    }
  }

  @Bean
  SecurityFilterChain security(
      HttpSecurity http,
      AuthenticationSuccessHandler success,
      AuthenticationFailureHandler failure,
      ActiveUserFilter activeUserFilter)
      throws Exception {

    http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/login",
                        "/admin/login",
                        "/register",
                        "/forgot-password",
                        "/reset-password",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/favicon.ico",
                        "/error")
                    .permitAll()
                    .requestMatchers("/admin/**", "/api/admin/**")
                    .hasRole("ADMIN")
                    .requestMatchers(
                        "/api/**", "/dashboard", "/expenses", "/categories", "/reports", "/profile")
                    .hasRole("USER")
                    .anyRequest()
                    .authenticated())
        .formLogin(
            form ->
                form.loginPage("/login")
                    .loginProcessingUrl("/login")
                    .usernameParameter("email")
                    .passwordParameter("password")
                    .successHandler(success)
                    .failureHandler(failure)
                    .permitAll())
        .logout(
            logout ->
                logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout")
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .permitAll())
        .csrf(
            csrf -> {
              CookieCsrfTokenRepository csrfRepository =
                  CookieCsrfTokenRepository.withHttpOnlyFalse();
              csrf.csrfTokenRepository(csrfRepository)
                  .csrfTokenRequestHandler(new HeaderFirstCsrfTokenRequestHandler());
            })
        .securityContext(context -> context.requireExplicitSave(false))
        .addFilterAfter(activeUserFilter, SecurityContextHolderFilter.class);

    return http.build();
  }
}
