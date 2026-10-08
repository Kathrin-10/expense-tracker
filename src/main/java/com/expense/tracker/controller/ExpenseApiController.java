package com.expense.tracker.controller;

import com.expense.tracker.dto.*;
import com.expense.tracker.service.ExpenseService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseApiController {
  private final ExpenseService service;

  public ExpenseApiController(ExpenseService service) {
    this.service = service;
  }

  @GetMapping
  public PageResponse<ExpenseResponse> search(
      @RequestParam(required = false) String title,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fromDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate toDate,
      @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "expenseDate") String sort,
      @RequestParam(defaultValue = "DESC") String direction) {
    var r = service.search(title, categoryId, fromDate, toDate, month, page, size, sort, direction);
    var total = service.filteredTotal(title, categoryId, fromDate, toDate, month);
    return new PageResponse<>(
        r.getContent(),
        r.getNumber(),
        r.getSize(),
        r.getTotalElements(),
        r.getTotalPages(),
        r.hasNext(),
        r.hasPrevious(),
        total);
  }

  @GetMapping("/{id}")
  public ExpenseResponse one(@PathVariable Long id) {
    return service.findById(id);
  }

  @PostMapping
  public ResponseEntity<ExpenseResponse> create(@Valid @RequestBody ExpenseRequest r) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));
  }

  @PutMapping("/{id}")
  public ExpenseResponse update(@PathVariable Long id, @Valid @RequestBody ExpenseRequest r) {
    return service.update(id, r);
  }

  @DeleteMapping("/{id}")
  public ApiMessage delete(@PathVariable Long id) {
    service.delete(id);
    return new ApiMessage("Expense deleted successfully");
  }

  public record PageResponse<T>(
      List<T> content,
      int page,
      int size,
      long totalElements,
      int totalPages,
      boolean hasNext,
      boolean hasPrevious,
      BigDecimal totalAmount) {}
}
