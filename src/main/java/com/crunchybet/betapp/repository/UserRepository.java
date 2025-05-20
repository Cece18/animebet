package com.crunchybet.betapp.repository;

import com.crunchybet.betapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);
    @Query("SELECT u.points FROM User u WHERE u.username = :username")
    Integer findPointsByUsername(@Param("username") String username);
}
