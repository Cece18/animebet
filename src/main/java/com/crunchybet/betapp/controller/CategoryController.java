package com.crunchybet.betapp.controller;

import com.crunchybet.betapp.dto.CategoryDTO;
import com.crunchybet.betapp.dto.CategoryOnlyDTO;
import com.crunchybet.betapp.dto.NomineeDTO;
import com.crunchybet.betapp.repository.CategoryRepository;
import com.crunchybet.betapp.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/all")
    public ResponseEntity<List<CategoryDTO>> getAllCategoriesandNominees() {
        List<CategoryDTO> categories = categoryService.getAllCategoriesWithNominees();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/categories")
    public List<CategoryOnlyDTO> getAllCategories() {
        List<CategoryOnlyDTO> categories = categoryService.getAllCategories();
        return categories;
    }
    @GetMapping("/{categoryName}/nominees")
    public ResponseEntity<?> getNomineesByCategoryName(@PathVariable String categoryName) {
        try {
            List<NomineeDTO> nominees = categoryService.getNomineesByCategoryName(categoryName);
            return ResponseEntity.ok(nominees);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/categories-with-nominees")
    public ResponseEntity<List<CategoryDTO>> getTopCategoriesWithNominees(
            @RequestParam(defaultValue = "4", required = false) int limit) {
        List<CategoryDTO> categories = categoryService.findTopNWithNominees(limit);
        return ResponseEntity.ok(categories);
    }

}
