package com.expense.tracker.controller;

import com.expense.tracker.dto.UserUpdateRequest;
import com.expense.tracker.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdminController {
  private final AuthService service;

  public AdminController(AuthService service) {
    this.service = service;
  }

  @GetMapping({"/admin", "/admin/customers"})
  public String customers() {
    return "admin-customers";
  }

  @GetMapping("/api/admin/customers")
  @ResponseBody
  public java.util.List<com.expense.tracker.dto.UserResponse> all() {
    return service.customers();
  }

  @GetMapping("/api/admin/customers/{id}")
  @ResponseBody
  public com.expense.tracker.dto.UserResponse one(@PathVariable Long id) {
    return service.find(id);
  }

  @PutMapping("/api/admin/customers/{id}")
  @ResponseBody
  public com.expense.tracker.dto.UserResponse update(
      @PathVariable Long id, @Valid @RequestBody UserUpdateRequest r) {
    return service.adminUpdate(id, r);
  }
}
