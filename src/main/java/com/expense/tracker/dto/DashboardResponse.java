package com.expense.tracker.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
    BigDecimal totalExpenses,
    long totalNumberOfExpenses,
    BigDecimal highestExpense,
    BigDecimal lowestExpense,
    BigDecimal currentMonthExpense,
    List<CategoryTotal> categoryTotals) {
  public record CategoryTotal(Long categoryId, String categoryName, BigDecimal total) {}
}
