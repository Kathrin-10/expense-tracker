package com.expense.tracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseResponse(
    Long id,
    String title,
    BigDecimal amount,
    LocalDate expenseDate,
    String description,
    Long categoryId,
    String categoryName,
    java.time.LocalDateTime createdAt,
    java.time.LocalDateTime updatedAt) {}
