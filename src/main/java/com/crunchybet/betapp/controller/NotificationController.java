package com.crunchybet.betapp.controller;


import com.crunchybet.betapp.dto.NotificationDTO;
import com.crunchybet.betapp.model.Notifications;
import com.crunchybet.betapp.model.User;
import com.crunchybet.betapp.service.NotificationService;
import com.crunchybet.betapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);


    @GetMapping
    public ResponseEntity<?> getNotifications() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                logger.error("No authentication found");
                return ResponseEntity.status(401).body(Map.of("error", "No authentication found"));
            }

            String username = authentication.getName();
            logger.info("Fetching notifications for user: {}", username);

            User user = userService.findByUsername(username);
            if (user == null) {
                logger.error("User not found: {}", username);
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }

            List<Notifications> notifications = notificationService.getUserNotifications(user);
            List<NotificationDTO> notificationDTOs = notifications.stream()
                    .map(notification -> {
                        try {
                            return NotificationDTO.fromNotification(notification);
                        } catch (Exception e) {
                            logger.error("Error converting notification to DTO: {}", e.getMessage());
                            return null;
                        }
                    })
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());

            logger.info("Retrieved {} notifications for user {}", notificationDTOs.size(), username);
            return ResponseEntity.ok(notificationDTOs);

        } catch (Exception e) {
            logger.error("Error getting notifications: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Error fetching notifications",
                    "details", e.getMessage()
            ));
        }
    }

    @GetMapping("/unread/count")
    public ResponseEntity<?> getUnreadCount() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Find the user by username
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }

            int count = notificationService.countUnreadNotifications(user);
            Map<String, Integer> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        try {
            logger.info("Marking notification {} as read" , id);
            notificationService.markAsRead(id);
            logger.info("Successfully marked notification {} as read" , id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            logger.info("Error marking notification {} as read: {}" , id , e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}
