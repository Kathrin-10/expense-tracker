package com.expense.tracker.service;

import com.expense.tracker.dto.*;
import com.expense.tracker.entity.*;
import com.expense.tracker.exception.ResourceNotFoundException;
import com.expense.tracker.repository.*;
import com.expense.tracker.security.CurrentUserService;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExpenseService {
  private final ExpenseRepository expenseRepository;
  private final CategoryRepository categoryRepository;
  private final CurrentUserService current;

  public ExpenseService(ExpenseRepository e, CategoryRepository c, CurrentUserService current) {
    expenseRepository = e;
    categoryRepository = c;
    this.current = current;
  }

  @Transactional(readOnly = true)
  public Page<ExpenseResponse> search(
      String title,
      Long categoryId,
      LocalDate fromDate,
      LocalDate toDate,
      YearMonth month,
      int page,
      int size,
      String sort,
      String direction) {
    if (month != null) {
      var s = month.atDay(1);
      var end = month.atEndOfMonth();
      if (fromDate == null || s.isAfter(fromDate)) fromDate = s;
      if (toDate == null || end.isBefore(toDate)) toDate = end;
    }
    if (fromDate != null && toDate != null && fromDate.isAfter(toDate))
      throw new IllegalArgumentException("From date cannot be after to date");
    var d = "ASC".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
    var p =
        PageRequest.of(
            Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(d, allowedSort(sort)));
    var u = current.get();
    return expenseRepository
        .search(u.getId(), blank(title), categoryId, fromDate, toDate, p)
        .map(this::toResponse);
  }

  @Transactional(readOnly = true)
  public BigDecimal filteredTotal(
      String title, Long categoryId, LocalDate fromDate, LocalDate toDate, YearMonth month) {
    if (month != null) {
      var s = month.atDay(1);
      var end = month.atEndOfMonth();
      if (fromDate == null || s.isAfter(fromDate)) fromDate = s;
      if (toDate == null || end.isBefore(toDate)) toDate = end;
    }
    if (fromDate != null && toDate != null && fromDate.isAfter(toDate))
      throw new IllegalArgumentException("From date cannot be after to date");
    return expenseRepository.searchTotal(
        current.get().getId(), blank(title), categoryId, fromDate, toDate);
  }

  @Transactional(readOnly = true)
  public ExpenseResponse findById(Long id) {
    return toResponse(owned(id));
  }

  public ExpenseResponse create(ExpenseRequest r) {
    Expense e = new Expense();
    apply(e, r);
    e.setUser(current.get());
    return toResponse(expenseRepository.save(e));
  }

  public ExpenseResponse update(Long id, ExpenseRequest r) {
    Expense e = owned(id);
    apply(e, r);
    return toResponse(e);
  }

  public void delete(Long id) {
    expenseRepository.delete(owned(id));
  }

  @Transactional(readOnly = true)
  public List<Expense> export(LocalDate from, LocalDate to) {
    return expenseRepository.findForExport(current.get().getId(), from, to);
  }

  private void apply(Expense e, ExpenseRequest r) {
    Category c =
        categoryRepository
            .findById(r.categoryId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Category with id " + r.categoryId() + " was not found"));
    e.setTitle(r.title().trim());
    e.setAmount(r.amount());
    e.setExpenseDate(r.expenseDate());
    e.setDescription(
        r.description() == null || r.description().isBlank() ? null : r.description().trim());
    e.setCategory(c);
  }

  private Expense owned(Long id) {
    var u = current.get();
    return expenseRepository
        .findById(id)
        .filter(e -> e.getUser() != null && e.getUser().getId().equals(u.getId()))
        .orElseThrow(
            () -> new ResourceNotFoundException("Expense with id " + id + " was not found"));
  }

  private String blank(String v) {
    return v == null || v.isBlank() ? null : v.trim();
  }

  private String allowedSort(String s) {
    return switch (s == null ? "" : s) {
      case "amount" -> "amount";
      case "title" -> "title";
      case "createdAt" -> "createdAt";
      default -> "expenseDate";
    };
  }

  private ExpenseResponse toResponse(Expense e) {
    return new ExpenseResponse(
        e.getId(),
        e.getTitle(),
        e.getAmount(),
        e.getExpenseDate(),
        e.getDescription(),
        e.getCategory().getId(),
        e.getCategory().getName(),
        e.getCreatedAt(),
        e.getUpdatedAt());
  }
}
