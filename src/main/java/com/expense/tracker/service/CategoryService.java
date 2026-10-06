package com.expense.tracker.service;

import com.expense.tracker.dto.*;
import com.expense.tracker.entity.Category;
import com.expense.tracker.exception.*;
import com.expense.tracker.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    public CategoryService(CategoryRepository categoryRepository, ExpenseRepository expenseRepository) { this.categoryRepository=categoryRepository; this.expenseRepository=expenseRepository; }
    @Transactional(readOnly=true)
    public List<CategoryResponse> findAll(){ return categoryRepository.findAll(org.springframework.data.domain.Sort.by("name")).stream().map(c -> new CategoryResponse(c.getId(),c.getName(),expenseRepository.countByCategoryId(c.getId()))).toList(); }
    @Transactional(readOnly=true)
    public CategoryResponse findById(Long id){ Category c=get(id); return new CategoryResponse(c.getId(),c.getName(),expenseRepository.countByCategoryId(id)); }
    public CategoryResponse create(CategoryRequest request){ String name=request.name().trim(); if(categoryRepository.existsByNameIgnoreCase(name)) throw new DuplicateCategoryException("Category '"+name+"' already exists"); Category c=categoryRepository.save(new Category(name)); return new CategoryResponse(c.getId(),c.getName(),0); }
    public CategoryResponse update(Long id, CategoryRequest request){ Category c=get(id); String name=request.name().trim(); if(categoryRepository.existsByNameIgnoreCaseAndIdNot(name,id)) throw new DuplicateCategoryException("Category '"+name+"' already exists"); c.setName(name); return new CategoryResponse(c.getId(),c.getName(),expenseRepository.countByCategoryId(id)); }
    public void delete(Long id){ get(id); if(expenseRepository.countByCategoryId(id)>0) throw new CategoryInUseException("Category cannot be deleted while it has expenses. Reassign or delete those expenses first."); categoryRepository.deleteById(id); }
    private Category get(Long id){ return categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category with id "+id+" was not found")); }
}
