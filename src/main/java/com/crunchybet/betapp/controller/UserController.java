package com.crunchybet.betapp.controller;

import com.crunchybet.betapp.model.User;
import com.crunchybet.betapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")


public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody String username,
                                    @RequestBody String password,
                                    @RequestBody String email) {
        User existingUser = userService.findByUsername(username);
        if (existingUser != null) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        User userWithEmail = userService.findByEmail(email);
        if (userWithEmail != null) {
            return ResponseEntity.badRequest().body("Email already in use");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        user.setEmail(email);

        userService.registerUser(user);

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody String username,
                                   @RequestBody String password) {
        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        if (!userService.verifyPassword(password, user.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid password");
        }

        return ResponseEntity.ok("Login successful");
    }
    @PostMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestBody String username,
                                            @RequestBody String oldPassword,
                                            @RequestBody String newPassword) {
        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        if (!userService.verifyPassword(oldPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid old password");
        }

        userService.updateUserPassword(user, newPassword);
        return ResponseEntity.ok("Password updated successfully");
    }
}
