package com.crunchybet.betapp.controller;

import com.crunchybet.betapp.dto.BetResponseDTO;
import com.crunchybet.betapp.dto.UserDTO;
import com.crunchybet.betapp.model.User;
import com.crunchybet.betapp.repository.UserRepository;
import com.crunchybet.betapp.service.JwtService;
import com.crunchybet.betapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody UserDTO userDTO) {
        User existingUser = userService.findByUsername(userDTO.getUsername());
        if (existingUser != null) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        User userWithEmail = userService.findByEmail(userDTO.getEmail());
        if (userWithEmail != null) {
            return ResponseEntity.badRequest().body("Email already in use");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());

        user.setEmail(userDTO.getEmail());

        userService.registerUser(user);

        return ResponseEntity.ok("User registered successfully");
    }


    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        try {
            User user = userService.findByEmail(email);
            if (user == null || !userService.verifyPassword(password, user.getPassword())) {
                return ResponseEntity.status(401).body("Invalid credentials");
            }

            UserDetails userDetails = userService.loadUserByUsername(email);
            String token = jwtService.generateToken(userDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("points", user.getPoints());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Authentication failed");
        }
    }

    @PostMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> payload) {
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        String oldPassword = payload.get("oldPassword");
        String newPassword = payload.get("newPassword");

        try {
            userService.updatePassword(currentUsername, oldPassword, newPassword);
            return ResponseEntity.ok("Password updated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("No authenticated user found");
        }

        User user = userService.findByUsername(userDetails.getUsername());
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("points", user.getPoints());
        response.put("initials", user.getUsername().substring(0, Math.min(2, user.getUsername().length())).toUpperCase());
        response.put("email", user.getEmail());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> payload) {
        // Get the authenticated username from Spring Security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        // Find the user by username
        User existingUser = userService.findByUsername(currentUsername);
        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        String newEmail = payload.get("email");
        String newUsername = payload.get("username");

        // Check if email already exists for another user
        User userWithEmail = userService.findByEmail(newEmail);
        if (userWithEmail != null && !userWithEmail.getUsername().equals(currentUsername)) {
            return ResponseEntity.badRequest().body("Email already in use by another account");
        }

        // Update user details
        existingUser.setEmail(newEmail);
        existingUser.setUsername(newUsername);

        User savedUser = userRepository.save(existingUser);

        Map<String, Object> response = new HashMap<>();
        response.put("username", savedUser.getUsername());
        response.put("email", savedUser.getEmail());

        return ResponseEntity.ok(response);
    }

    // In UserController.java
    @GetMapping("/betting-history")
    public ResponseEntity<?> getBettingHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        List<BetResponseDTO> history = userService.getBettingHistory(username);
        return ResponseEntity.ok(history);
    }
}
