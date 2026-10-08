package com.expense.tracker.controller;

import com.expense.tracker.exception.CategoryInUseException;
import com.expense.tracker.exception.DuplicateCategoryException;
import com.expense.tracker.exception.DuplicateUserException;
import com.expense.tracker.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiError> notFound(
      ResourceNotFoundException exception, HttpServletRequest request) {
    return build(HttpStatus.NOT_FOUND, "Resource Not Found", exception.getMessage(), request);
  }

  @ExceptionHandler({
    DuplicateCategoryException.class,
    DuplicateUserException.class,
    CategoryInUseException.class
  })
  public ResponseEntity<ApiError> conflict(RuntimeException exception, HttpServletRequest request) {
    return build(HttpStatus.CONFLICT, "Conflict", exception.getMessage(), request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> validation(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    String message =
        exception.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));
    return build(HttpStatus.BAD_REQUEST, "Validation Failed", message, request);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiError> dataIntegrity(
      DataIntegrityViolationException exception, HttpServletRequest request) {
    return build(
        HttpStatus.CONFLICT,
        "Database Constraint Violation",
        "The requested change conflicts with existing data.",
        request);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiError> illegalArgument(
      IllegalArgumentException exception, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, "Invalid Request", exception.getMessage(), request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> internal(Exception exception, HttpServletRequest request) {
    return build(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Internal Server Error",
        "Something went wrong while processing your request.",
        request);
  }

  private ResponseEntity<ApiError> build(
      HttpStatus status, String error, String message, HttpServletRequest request) {
    return ResponseEntity.status(status)
        .body(
            new ApiError(
                LocalDateTime.now(), status.value(), error, message, request.getRequestURI()));
  }

  public record ApiError(
      LocalDateTime timestamp, int status, String error, String message, String path) {}
}
