package com.crunchybet.betapp.repository;

import com.crunchybet.betapp.model.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository  extends JpaRepository<Category, Long> {

    List<Category> findByNameOrderByNameAsc(String name);

    @Query("SELECT c FROM Category c ORDER BY c.id ASC")
    List<Category> findTopNOrderByIdAsc(Pageable pageable);

    List<Category> findAllByOrderById();

}
