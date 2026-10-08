package com.expense.tracker.controller;

import com.expense.tracker.dto.*;
import com.expense.tracker.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
public class CategoryApiController {
  private final CategoryService service;

  public CategoryApiController(CategoryService service) {
    this.service = service;
  }

  @GetMapping
  public List<CategoryResponse> all() {
    return service.findAll();
  }

  @GetMapping("/{id}")
  public CategoryResponse one(@PathVariable Long id) {
    return service.findById(id);
  }

  @PostMapping
  public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest r) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));
  }

  @PutMapping("/{id}")
  public CategoryResponse update(@PathVariable Long id, @Valid @RequestBody CategoryRequest r) {
    return service.update(id, r);
  }

  @DeleteMapping("/{id}")
  public ApiMessage delete(@PathVariable Long id) {
    service.delete(id);
    return new ApiMessage("Category deleted successfully");
  }
}
