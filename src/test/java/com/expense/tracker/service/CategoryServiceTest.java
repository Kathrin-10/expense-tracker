package com.expense.tracker.service;

import com.expense.tracker.dto.CategoryRequest;
import com.expense.tracker.entity.Category;
import com.expense.tracker.exception.DuplicateCategoryException;
import com.expense.tracker.repository.CategoryRepository;
import com.expense.tracker.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock CategoryRepository categoryRepository;
    @Mock ExpenseRepository expenseRepository;
    @InjectMocks CategoryService service;

    @Test
    void createRejectsDuplicateCategoryName() {
        when(categoryRepository.existsByNameIgnoreCase("Food")).thenReturn(true);

        assertThrows(DuplicateCategoryException.class,
                () -> service.create(new CategoryRequest("Food")));
    }

    @Test
    void createAcceptsUniqueCategoryName() {
        when(categoryRepository.existsByNameIgnoreCase("Travel")).thenReturn(false);
        when(categoryRepository.save(org.mockito.ArgumentMatchers.any(Category.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.create(new CategoryRequest("Travel"));
    }
}
