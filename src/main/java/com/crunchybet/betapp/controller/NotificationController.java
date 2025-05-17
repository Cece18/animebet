package com.crunchybet.betapp.controller;


import com.crunchybet.betapp.dto.NotificationDTO;
import com.crunchybet.betapp.model.Notifications;
import com.crunchybet.betapp.model.User;
import com.crunchybet.betapp.service.NotificationService;
import com.crunchybet.betapp.service.UserService;
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

    @GetMapping
    public ResponseEntity<?> getNotifications() {
        try {
            // Get the authenticated username from Spring Security
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            // Find the user by username
            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }

            List<Notifications> notifications = notificationService.getUserNotifications(user);
            List<NotificationDTO> notificationDTOs = notifications.stream()
                    .map(NotificationDTO::fromNotification)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(notificationDTOs);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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
            notificationService.markAsRead(id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}
