package com.expense.tracker.service;

import com.expense.tracker.dto.ExpenseRequest;
import com.expense.tracker.dto.ExpenseResponse;
import com.expense.tracker.entity.Category;
import com.expense.tracker.entity.Expense;
import com.expense.tracker.exception.ResourceNotFoundException;
import com.expense.tracker.repository.CategoryRepository;
import com.expense.tracker.repository.ExpenseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;

    public ExpenseService(ExpenseRepository expenseRepository, CategoryRepository categoryRepository) {
        this.expenseRepository = expenseRepository;
        this.categoryRepository = categoryRepository;
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
            LocalDate monthStart = month.atDay(1);
            LocalDate monthEnd = month.atEndOfMonth();
            if (fromDate == null || monthStart.isAfter(fromDate)) {
                fromDate = monthStart;
            }
            if (toDate == null || monthEnd.isBefore(toDate)) {
                toDate = monthEnd;
            }
        }

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("From date cannot be after to date");
        }

        Sort.Direction sortDirection = "ASC".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Sort sortBy = Sort.by(sortDirection, allowedSort(sort));
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        return expenseRepository.search(
                        blankToNull(title), categoryId, fromDate, toDate,
                        PageRequest.of(safePage, safeSize, sortBy))
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse findById(Long id) {
        return toResponse(get(id));
    }

    public ExpenseResponse create(ExpenseRequest request) {
        Expense expense = new Expense();
        apply(expense, request);
        return toResponse(expenseRepository.save(expense));
    }

    public ExpenseResponse update(Long id, ExpenseRequest request) {
        Expense expense = get(id);
        apply(expense, request);
        return toResponse(expense);
    }

    public void delete(Long id) {
        get(id);
        expenseRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Expense> export(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null) {
            fromDate = LocalDate.of(2000, 1, 1);
        }
        if (toDate == null) {
            toDate = LocalDate.now();
        }
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("From date cannot be after to date");
        }
        return expenseRepository.findForExport(fromDate, toDate);
    }

    private void apply(Expense expense, ExpenseRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category with id " + request.categoryId() + " was not found"));

        expense.setTitle(request.title().trim());
        expense.setAmount(request.amount());
        expense.setExpenseDate(request.expenseDate());
        expense.setDescription(request.description() == null || request.description().isBlank()
                ? null
                : request.description().trim());
        expense.setCategory(category);
    }

    private Expense get(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Expense with id " + id + " was not found"));
    }

    private ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(), expense.getTitle(), expense.getAmount(),
                expense.getExpenseDate(), expense.getDescription(),
                expense.getCategory().getId(), expense.getCategory().getName());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String allowedSort(String sort) {
        return switch (sort == null ? "expenseDate" : sort) {
            case "amount", "title", "expenseDate", "createdAt" -> sort;
            default -> "expenseDate";
        };
    }
}
