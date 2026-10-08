package com.expense.tracker.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
    @NotBlank @Size(max = 100) String fullName,
    @NotBlank @Email @Size(max = 150) String email,
    @Size(max = 30) String phone,
    @NotBlank @Size(min = 8, max = 100) String password) {}
