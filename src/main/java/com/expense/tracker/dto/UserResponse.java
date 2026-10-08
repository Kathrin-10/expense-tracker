package com.expense.tracker.dto;

import com.expense.tracker.entity.Role;

public record UserResponse(
    Long id, String fullName, String email, String phone, Role role, boolean active) {}
