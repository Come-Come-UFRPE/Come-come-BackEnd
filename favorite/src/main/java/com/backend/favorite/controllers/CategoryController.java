package com.backend.favorite.controllers;

import com.backend.favorite.dtos.CategoryDTO;
import com.backend.favorite.dtos.CategoryPatchDto;
import com.backend.favorite.models.Category;
import com.backend.favorite.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    final
    CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/create")
    public ResponseEntity<Category> createCategory(@RequestBody CategoryDTO categoryDTO) {

        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.addCategory(categoryDTO));

    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Category>> getCategoriesByOwnerId(@PathVariable UUID ownerId) {
        return ResponseEntity.ok(categoryService.getCategoryByOwnerId(ownerId));
    }

    @GetMapping("/id/{categoryId}")
    public ResponseEntity<Category> getCategoryById(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(categoryService.getCategoryById(categoryId));
    }

    @PatchMapping("/update")
    public ResponseEntity<Category> updateCategory(@RequestBody CategoryPatchDto categoryDTO) {
        return ResponseEntity.ok(categoryService.updateCategoryName(categoryDTO));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }

}
