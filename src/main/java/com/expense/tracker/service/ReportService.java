package com.expense.tracker.service;

import com.expense.tracker.dto.MonthlyReportResponse;
import com.expense.tracker.repository.ExpenseRepository;
import com.expense.tracker.security.CurrentUserService;
import java.math.BigDecimal;
import java.time.Month;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class ReportService {
  private final ExpenseRepository repo;
  private final CurrentUserService current;

  public ReportService(ExpenseRepository r, CurrentUserService c) {
    repo = r;
    current = c;
  }

  public List<MonthlyReportResponse> monthly() {
    return repo.monthlyTotals(current.get().getId()).stream()
        .map(
            r -> {
              int y = ((Number) r[0]).intValue(), m = ((Number) r[1]).intValue();
              return new MonthlyReportResponse(
                  y, m, Month.of(m).name(), (BigDecimal) r[2], ((Number) r[3]).longValue());
            })
        .toList();
  }
}
