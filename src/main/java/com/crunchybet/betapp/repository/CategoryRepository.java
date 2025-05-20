package com.crunchybet.betapp.repository;

import com.crunchybet.betapp.model.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository  extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.nominees n WHERE LOWER(c.name) = LOWER(:name) ORDER BY n.id ASC")
    List<Category> findByNameWithNominees(@Param("name") String name);


    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.nominees ORDER BY c.id ASC")
    List<Category> findTopNOrderByIdAsc(Pageable pageable);




}
