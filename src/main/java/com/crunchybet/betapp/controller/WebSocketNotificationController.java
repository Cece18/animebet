package com.crunchybet.betapp.controller;

import com.crunchybet.betapp.dto.NotificationDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.crunchybet.betapp.dto.PointsUpdateDTO;

@Controller
public class WebSocketNotificationController {
    private final SimpMessagingTemplate messagingTemplate;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketNotificationController.class);

    public WebSocketNotificationController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // In WebSocketNotificationController.java
    public void sendPointsUpdate(String username, int points, String reason) {
        try {
            logger.info("Sending points update to user {}: {} points ({})", username, points, reason);
            messagingTemplate.convertAndSendToUser(
                    username,
                    "/topic/points",
                    new PointsUpdateDTO(points, reason)
            );
            logger.debug("Successfully sent points update to user {}", username);
        } catch (Exception e) {
            // Log the error but don't throw it to prevent disrupting the main flow
            logger.error("Failed to send points update to user {}: {}", username, e.getMessage(), e);
            System.err.println("Failed to send points update: " + e.getMessage());
        }
    }

    public void sendNotification(String username, NotificationDTO notification) {
        try {
            logger.info("Sending notification to user {}: {}", username, notification.getMessage());
            messagingTemplate.convertAndSendToUser(
                    username,
                    "/topic/notifications",
                    notification
            );
            logger.debug("Successfully sent notification to user {}", username);
        } catch (Exception e) {
            logger.error("Failed to send notification to user {}: {}", username, e.getMessage(), e);
        }
    }
}
