package com.crunchybet.betapp.controller;

import com.crunchybet.betapp.dto.BetResponseDTO;
import com.crunchybet.betapp.dto.UserDTO;
import com.crunchybet.betapp.model.User;
import com.crunchybet.betapp.repository.UserRepository;
import com.crunchybet.betapp.service.JwtService;
import com.crunchybet.betapp.service.SseService;
import com.crunchybet.betapp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

    @Autowired
    private SseService sseService;

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);


    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody UserDTO userDTO) {
        try {
            userService.createUser(userDTO);
            return ResponseEntity.ok("User registered successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Something went wrong");
        }
    }


    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        try {
            System.out.println("Attempting login for: " + email);

            User user = userService.findByEmail(email);
            System.out.println("User found: " + (user != null));

            if (user == null) {
                return ResponseEntity.status(401).body("Invalid credentials - user not found");
            }

            boolean match = userService.verifyPassword(password, user.getPassword());
            System.out.println("Password match? " + match);

            if (!match) {
                return ResponseEntity.status(401).body("Invalid credentials - password mismatch");
            }

            System.out.println("Loading user details...");
            UserDetails userDetails = userService.loadUserByUsername(email);
            System.out.println("UserDetails loaded: " + userDetails.getUsername());

            System.out.println("Generating token...");
            String token = jwtService.generateToken(userDetails);
            System.out.println("Token generated: " + token);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("points", user.getPoints());
            response.put("Role", user.getRole());

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

    @PostMapping("/change-password")
    public ResponseEntity<?> Password(@RequestBody Map<String, String> payload) {
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


    @Transactional(readOnly = true)
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
        response.put("role", user.getRole());

        return ResponseEntity.ok(response);
    }

    @Transactional(readOnly = true)
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

        // Only validate email if it's being changed
        if (!newEmail.equals(existingUser.getEmail())) {
            User userWithEmail = userService.findByEmail(newEmail);
            if (userWithEmail != null && !userWithEmail.getId().equals(existingUser.getId())) {
                return ResponseEntity.badRequest().body("Email already in use by another account");
            }
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

    @GetMapping("/bets/category/{categoryId}")
    public ResponseEntity<?> getUserBetsByCategory(@PathVariable Long categoryId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUsername(username);

            if (user == null) {
                return ResponseEntity.status(401).body("User not authenticated");
            }

            List<BetResponseDTO> bets = userService.getBetsByCategory(username, categoryId);
            return ResponseEntity.ok(bets);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Transactional(readOnly = true)
    @GetMapping("/points")
    public ResponseEntity<?> getUserPoints() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Integer points = userRepository.findPointsByUsername(username);
        if (points == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        return ResponseEntity.ok(Map.of("points", points));
    }



    @GetMapping(value = "/points-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamPoints() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        String email = userService.findByUsername(username).getEmail();
        logger.info("👂 SSE connection established for user: {}", email);
        return sseService.createEmitter(email);
    }

    // In UserController.java
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            userService.initiatePasswordReset(email);
            return ResponseEntity.ok(Map.of("message", "Reset code sent to email"));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        }
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<?> verifyResetCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        boolean isValid = userService.verifyResetCode(email, code);
        if (isValid) {
            return ResponseEntity.ok(Map.of("message", "Code verified successfully"));
        } else {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid or expired code"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        String newPassword = request.get("newPassword");

        try {
            userService.resetPassword(email, code, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
