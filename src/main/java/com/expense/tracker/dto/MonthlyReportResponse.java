package com.expense.tracker.dto;

import java.math.BigDecimal;

public record MonthlyReportResponse(int year, int month, String monthName, BigDecimal totalExpenses, long numberOfExpenses) {}
