package com.crunchybet.betapp.controller;

import com.crunchybet.betapp.dto.BetResponseDTO;
import com.crunchybet.betapp.dto.UpdateBetRequest;
import com.crunchybet.betapp.model.User;
import com.crunchybet.betapp.service.BetService;
import com.crunchybet.betapp.dto.PlaceBetRequest;
import com.crunchybet.betapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bets")
public class BetController {
    @Autowired
    private BetService betService;

    @Autowired
    private UserService userService;


    //Place a bet
    @PostMapping("/place-bet")
    public ResponseEntity<?> placeBet(@RequestBody PlaceBetRequest request) {
        try {
            // Get authentication and
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            System.out.println("Username from token: " + username); // Debug line


            User user = userService.findByUsername(username);

            System.out.println("User found: " + (user != null)); // Debug line


            if(user == null) {
                return ResponseEntity.status(401).body("User not authenticated or not found");
            }

            betService.placeBet(request.getCategoryId(), request.getNomineeId(), user.getId(), request.getAmount());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Bet placed successfully!");
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("success", false);
            return ResponseEntity.badRequest().body(response);
        }
    }

    //update a bet
    @PutMapping("/update-bet/{betId}")
    public ResponseEntity<?> updateBet(@PathVariable Long betId, @RequestBody UpdateBetRequest request) {
        try {

            // Get authentication and username
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            User user = userService.findByUsername(username);

            if(user == null) {
                return ResponseEntity.status(401).body("User not authenticated or not found");
            }

            betService.updateBet(betId, request.getNomineeId(), user.getId(), request.getNewAmount());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Bet updated successfully!");
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("success", false);
            return ResponseEntity.badRequest().body(response);
        }
    }


    @GetMapping("/check-existing-bet")
    public ResponseEntity<?> checkExistingBet(@RequestParam Long categoryId,
                                              @RequestParam Long nomineeId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUsername(username);

            if (user == null) {
                return ResponseEntity.status(401).body("User not authenticated");
            }

            boolean hasExistingBet = betService.hasExistingBet(user.getId(), categoryId, nomineeId);
            Long existingBetId = hasExistingBet ?
                    betService.getExistingBetId(user.getId(), categoryId, nomineeId) : null;

            Map<String, Object> response = new HashMap<>();
            response.put("hasExistingBet", hasExistingBet);
            response.put("existingBetId", existingBetId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/delete-bet/{betId}")
    public ResponseEntity<?> deleteBet(@PathVariable Long betId) {
        try {
            // Get authentication and username
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUsername(username);

            if (user == null) {
                return ResponseEntity.status(401).body("User not authenticated");
            }

            betService.deleteBet(betId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Bet deleted successfully");
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            response.put("success", false);
            return ResponseEntity.badRequest().body(response);
        }
    }

}
