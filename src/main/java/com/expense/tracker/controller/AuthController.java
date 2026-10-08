package com.expense.tracker.controller;

import com.expense.tracker.dto.RegisterRequest;
import com.expense.tracker.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {
  private final AuthService service;

  public AuthController(AuthService service) {
    this.service = service;
  }

  @GetMapping("/login")
  public String login() {
    return "login";
  }

  @GetMapping("/admin/login")
  public String adminLogin() {
    return "admin-login";
  }

  @GetMapping("/register")
  public String register(Model model) {
    model.addAttribute("register", new RegisterRequest("", "", "", ""));
    return "register";
  }

  @PostMapping("/register")
  public String register(
      @Valid @ModelAttribute("register") RegisterRequest request,
      BindingResult result,
      Model model) {
    if (result.hasErrors()) return "register";
    try {
      service.register(request);
      return "redirect:/login?registered=true";
    } catch (RuntimeException e) {
      model.addAttribute("error", e.getMessage());
      return "register";
    }
  }

  @GetMapping("/forgot-password")
  public String forgot() {
    return "forgot-password";
  }

  @PostMapping("/forgot-password")
  public String forgot(@RequestParam String email, Model model) {
    try {
      service.createResetToken(email);
      model.addAttribute(
          "message",
          "If an account exists for that email, a password reset link will be sent shortly.");
    } catch (IllegalStateException e) {
      model.addAttribute("error", e.getMessage());
    } catch (org.springframework.mail.MailException e) {
      model.addAttribute(
          "error", "We couldn't send the reset email right now. Please try again later.");
    }
    return "forgot-password";
  }

  @GetMapping("/reset-password")
  public String reset(@RequestParam String token, Model model) {
    if (!service.validToken(token)) {
      model.addAttribute("error", "This reset link is invalid or has expired.");
    }
    model.addAttribute("token", token);
    return "reset-password";
  }

  @PostMapping("/reset-password")
  public String reset(
      @RequestParam String token,
      @RequestParam String password,
      @RequestParam String confirmPassword,
      Model model) {
    model.addAttribute("token", token);
    if (!password.equals(confirmPassword)) {
      model.addAttribute("error", "Passwords do not match.");
      return "reset-password";
    }
    try {
      service.resetPassword(token, password);
      return "redirect:/login?reset=true";
    } catch (RuntimeException e) {
      model.addAttribute("error", e.getMessage());
      return "reset-password";
    }
  }
}
