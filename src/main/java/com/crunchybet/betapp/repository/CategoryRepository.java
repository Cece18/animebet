package com.crunchybet.betapp.repository;

import com.crunchybet.betapp.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository  extends JpaRepository<Category, Long> {
    // Custom query methods can be defined here if needed
    // For example, find categories by name or other attributes
    List<Category> findByName(String name);
    List<Category> findByDescription(String description);
}
