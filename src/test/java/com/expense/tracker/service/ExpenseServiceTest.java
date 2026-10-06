package com.expense.tracker.service;

import com.expense.tracker.dto.ExpenseRequest;
import com.expense.tracker.entity.Category;
import com.expense.tracker.exception.ResourceNotFoundException;
import com.expense.tracker.repository.CategoryRepository;
import com.expense.tracker.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock ExpenseRepository expenseRepository;
    @Mock CategoryRepository categoryRepository;
    @InjectMocks ExpenseService service;

    @Test
    void createRejectsUnknownCategory() {
        when(categoryRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        ExpenseRequest request = new ExpenseRequest(
                "Lunch", BigDecimal.valueOf(250), LocalDate.now(), null, 999L);

        assertThrows(ResourceNotFoundException.class, () -> service.create(request));
    }
}
