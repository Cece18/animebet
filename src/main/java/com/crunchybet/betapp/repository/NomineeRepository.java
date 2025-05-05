package com.crunchybet.betapp.repository;

import com.crunchybet.betapp.model.Category;
import com.crunchybet.betapp.model.Nominee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NomineeRepository  extends JpaRepository<Nominee, Long> {
    // Custom query methods can be defined here if needed
    // For example, find categories by name or other attributes
    List<Category> findByName(String name);
    List<Category> findByCategoryId(Long id);
}
