package com.expense.tracker.service;

import com.expense.tracker.dto.CategoryRequest;
import com.expense.tracker.dto.CategoryResponse;
import com.expense.tracker.entity.Category;
import com.expense.tracker.exception.CategoryInUseException;
import com.expense.tracker.exception.DuplicateCategoryException;
import com.expense.tracker.exception.ResourceNotFoundException;
import com.expense.tracker.repository.CategoryRepository;
import com.expense.tracker.repository.ExpenseRepository;
import com.expense.tracker.security.CurrentUserService;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryService {
  private final CategoryRepository categoryRepository;
  private final ExpenseRepository expenseRepository;
  private final CurrentUserService currentUser;

  public CategoryService(
      CategoryRepository categoryRepository,
      ExpenseRepository expenseRepository,
      CurrentUserService currentUser) {
    this.categoryRepository = categoryRepository;
    this.expenseRepository = expenseRepository;
    this.currentUser = currentUser;
  }

  @Transactional(readOnly = true)
  public List<CategoryResponse> findAll() {
    Long userId = currentUser.get().getId();
    return categoryRepository.findAll(Sort.by("name")).stream()
        .map(
            c ->
                new CategoryResponse(
                    c.getId(),
                    c.getName(),
                    expenseRepository.countByCategoryIdAndUserId(c.getId(), userId)))
        .toList();
  }

  @Transactional(readOnly = true)
  public CategoryResponse findById(Long id) {
    Category c = get(id);
    long count = expenseRepository.countByCategoryIdAndUserId(id, currentUser.get().getId());
    return new CategoryResponse(c.getId(), c.getName(), count);
  }

  public CategoryResponse create(CategoryRequest request) {
    String name = request.name().trim();
    if (categoryRepository.existsByNameIgnoreCase(name)) {
      throw new DuplicateCategoryException("Category '" + name + "' already exists");
    }
    Category c = categoryRepository.save(new Category(name));
    return new CategoryResponse(c.getId(), c.getName(), 0);
  }

  public CategoryResponse update(Long id, CategoryRequest request) {
    Category c = get(id);
    String name = request.name().trim();
    if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
      throw new DuplicateCategoryException("Category '" + name + "' already exists");
    }
    c.setName(name);
    long count = expenseRepository.countByCategoryIdAndUserId(id, currentUser.get().getId());
    return new CategoryResponse(c.getId(), c.getName(), count);
  }

  public void delete(Long id) {
    get(id);
    if (expenseRepository.countByCategoryIdAndUserId(id, currentUser.get().getId()) > 0) {
      throw new CategoryInUseException(
          "Category cannot be deleted while you have expenses using it.");
    }
    categoryRepository.deleteById(id);
  }

  private Category get(Long id) {
    return categoryRepository
        .findById(id)
        .orElseThrow(
            () -> new ResourceNotFoundException("Category with id " + id + " was not found"));
  }
}
