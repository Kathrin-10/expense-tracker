package com.expense.tracker.dto;

import jakarta.validation.constraints.*;

public record UserUpdateRequest(
    @NotBlank @Size(max = 100) String fullName,
    @NotBlank @Email @Size(max = 150) String email,
    @Size(max = 30) String phone,
    Boolean active) {}
