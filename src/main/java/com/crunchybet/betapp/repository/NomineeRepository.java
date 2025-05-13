package com.crunchybet.betapp.repository;

import com.crunchybet.betapp.model.Category;
import com.crunchybet.betapp.model.Nominee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NomineeRepository  extends JpaRepository<Nominee, Long> {
    List<Nominee> findByName(String name);
    List<Nominee> findByDescription(String description);
}
