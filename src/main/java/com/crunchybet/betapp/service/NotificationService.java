package com.crunchybet.betapp.service;


import com.crunchybet.betapp.controller.WebSocketNotificationController;
import com.crunchybet.betapp.dto.NotificationDTO;
import com.crunchybet.betapp.model.Category;
import com.crunchybet.betapp.model.Notifications;
import com.crunchybet.betapp.model.User;
import com.crunchybet.betapp.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {


    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private WebSocketNotificationController webSocketController;

    @Transactional
    public Notifications createBetResultNotification(User user, Category category, boolean won, double amount) {
        String message = won ?
                String.format("Congratulations! You won %.2f points in the %s category!", amount, category.getName()) :
                String.format("Better luck next time! You lost your bet in the %s category.", category.getName());

        String actionUrl = "/categories/" + category.getId();

        Notifications notification = new Notifications(user, message,actionUrl, false, LocalDateTime.now());

        notification = notificationRepository.save(notification);

        // Send WebSocket notification
        webSocketController.sendNotification(
                user.getUsername(),
                NotificationDTO.fromNotification(notification)
        );

        return notification;
    }

    public List<Notifications> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public int countUnreadNotifications(User user) {
        return notificationRepository.countByUserAndReadFalse(user);
    }


    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }
}
