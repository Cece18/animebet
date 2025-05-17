package com.crunchybet.betapp.controller;

import com.crunchybet.betapp.dto.CategoryDTO;
import com.crunchybet.betapp.dto.CategoryOnlyDTO;
import com.crunchybet.betapp.dto.NomineeDTO;
import com.crunchybet.betapp.repository.CategoryRepository;
import com.crunchybet.betapp.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/closeAll")
    @PreAuthorize("hasRole('ADMIN')")  // Ensure only admin can access
    public ResponseEntity<?> closeAllCategories() {
        try {
            categoryService.closeAllCategories();
            return ResponseEntity.ok(Map.of(
                    "message", "All categories closed successfully",
                    "success", true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "success", false
            ));
        }
    }


    @PostMapping("/{categoryId}/set-winner")  // Change to PostMapping if you prefer POST
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> setCategoryWinner(@PathVariable Long categoryId, @RequestParam Long nomineeId) {
        try {
            categoryService.setWinnerAndProcessBets(categoryId, nomineeId);
            return ResponseEntity.ok(Map.of(
                    "message", "Winner set successfully and bets processed",
                    "success", true
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "success", false
            ));
        }
    }

}
