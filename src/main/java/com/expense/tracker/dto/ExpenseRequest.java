package com.expense.tracker.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseRequest(
    @NotBlank(message = "Title is required")
        @Size(max = 120, message = "Title cannot exceed 120 characters")
        String title,
    @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        @Digits(integer = 12, fraction = 2, message = "Amount must have at most 2 decimal places")
        BigDecimal amount,
    @NotNull(message = "Expense date is required")
        @PastOrPresent(message = "Expense date cannot be in the future")
        LocalDate expenseDate,
    @Size(max = 250, message = "Description cannot exceed 250 characters") String description,
    @NotNull(message = "Category is required") Long categoryId) {}
