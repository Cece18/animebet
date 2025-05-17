package com.crunchybet.betapp.controller;

import com.crunchybet.betapp.dto.NotificationDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketNotificationController {
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendNotification(String username, NotificationDTO notification) {
        messagingTemplate.convertAndSendToUser(
                username,
                "/topic/notifications",
                notification
        );
    }
}
