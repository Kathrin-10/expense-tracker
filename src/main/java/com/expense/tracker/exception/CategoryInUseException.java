package com.expense.tracker.exception;

public class CategoryInUseException extends RuntimeException {
  public CategoryInUseException(String message) {
    super(message);
  }
}
