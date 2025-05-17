package com.crunchybet.betapp.repository;


import com.crunchybet.betapp.model.Notifications;
import com.crunchybet.betapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notifications, Long> {
    List<Notifications> findByUserOrderByCreatedAtDesc(User user);
    List<Notifications> findByUserAndReadFalseOrderByCreatedAtDesc(User user);
    int countByUserAndReadFalse(User user);

}
