package com.expense.tracker.controller;

import com.expense.tracker.dto.MonthlyReportResponse;
import com.expense.tracker.service.ReportService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportApiController {
  private final ReportService service;

  public ReportApiController(ReportService service) {
    this.service = service;
  }

  @GetMapping("/monthly")
  public List<MonthlyReportResponse> monthly() {
    return service.monthly();
  }
}
