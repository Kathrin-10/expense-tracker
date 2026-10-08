package com.expense.tracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
  @GetMapping({"/", "/dashboard"})
  public String dashboard() {
    return "dashboard";
  }

  @GetMapping("/expenses")
  public String expenses() {
    return "expenses";
  }

  @GetMapping("/categories")
  public String categories() {
    return "categories";
  }

  @GetMapping("/reports")
  public String reports() {
    return "reports";
  }
}
