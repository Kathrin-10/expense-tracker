package com.expense.tracker.controller;

import com.expense.tracker.dto.DashboardResponse;
import com.expense.tracker.service.DashboardService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {
  private final DashboardService service;

  public DashboardApiController(DashboardService service) {
    this.service = service;
  }

  @GetMapping
  public DashboardResponse dashboard() {
    return service.getDashboard();
  }
}
