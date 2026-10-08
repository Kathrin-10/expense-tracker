package com.expense.tracker.service;

import com.expense.tracker.dto.DashboardResponse;
import com.expense.tracker.repository.ExpenseRepository;
import com.expense.tracker.security.CurrentUserService;
import java.math.BigDecimal;
import java.time.YearMonth;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
  private final ExpenseRepository repo;
  private final CurrentUserService current;

  public DashboardService(ExpenseRepository r, CurrentUserService c) {
    repo = r;
    current = c;
  }

  public DashboardResponse getDashboard() {
    Long id = current.get().getId();
    YearMonth ym = YearMonth.now();
    long count = repo.countByUserId(id);
    BigDecimal total = repo.totalAmountByUserId(id),
        max = repo.maxAmount(id),
        min = count == 0 ? BigDecimal.ZERO : repo.minAmount(id),
        currentMonth = repo.totalBetween(id, ym.atDay(1), ym.atEndOfMonth());
    var cats =
        repo.totalsByCategory(id).stream()
            .map(
                r ->
                    new DashboardResponse.CategoryTotal(
                        (Long) r[0], (String) r[1], (BigDecimal) r[2]))
            .toList();
    return new DashboardResponse(total, count, max, min, currentMonth, cats);
  }
}
