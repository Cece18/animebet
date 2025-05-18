package com.crunchybet.betapp.service;

import com.crunchybet.betapp.controller.WebSocketNotificationController;
import com.crunchybet.betapp.dto.BetResponseDTO;
import com.crunchybet.betapp.model.Bet;
import com.crunchybet.betapp.model.Category;
import com.crunchybet.betapp.model.Nominee;
import com.crunchybet.betapp.model.User;
import com.crunchybet.betapp.model.enums.BetStatus;
import com.crunchybet.betapp.repository.BetRepository;
import com.crunchybet.betapp.repository.CategoryRepository;
import com.crunchybet.betapp.repository.NomineeRepository;
import com.crunchybet.betapp.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BetService {
    @Autowired
    BetRepository betRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    NomineeRepository nomineeRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebSocketNotificationController webSocketController;

    @Transactional
    public Bet placeBet(Long categoryId, Long nomineeId, Long userId, Integer amount) {
        // Check if category and nominee exist
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        Nominee nominee = nomineeRepository.findById(nomineeId)
                .orElseThrow(() -> new RuntimeException("Nominee not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if the user has enough balance
        if (user.getPoints() < amount) {
            throw new RuntimeException("Insufficient balance");
        }

        // Check if the category is active
        if (!category.isActive()) {
            throw new RuntimeException("Category is closed for betting");
        }

        // Deduct the amount from user's points
        user.setPoints(user.getPoints() - amount);
        userRepository.save(user);



        webSocketController.sendPointsUpdate(
                user.getUsername(),
                user.getPoints(),
                "Bet placed: -" + amount + " points"
        );

        // Create new bet with all required fields
        Bet bet = new Bet();
        bet.setCategory(category);
        bet.setNominee(nominee);
        bet.setAmount(amount);
        bet.setStatus(BetStatus.PENDING);
        bet.setWinningAmount(0.0);
        bet.setPlacedAt(LocalDateTime.now());
        bet.setCreatedAt(LocalDateTime.now());
        bet.setUser(user);

        betRepository.save(bet);

        return bet;
    }

    public void updateBet(Long betId, Long nomineeId, Long userId, Integer newAmount) {
        Bet existingBet = betRepository.findById(betId)
                .orElseThrow(() -> new RuntimeException("Bet not found"));

        // Verify the bet belongs to the user
        if (!existingBet.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to update this bet");
        }

        Nominee nominee = nomineeRepository.findById(nomineeId)
                .orElseThrow(() -> new RuntimeException("Nominee not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if the category is active
        if (!existingBet.getCategory().isActive()) {
            throw new RuntimeException("Cannot update Bet, Category is closed for betting");
        }

        // Calculate points difference
        int pointsDifference = newAmount - existingBet.getAmount();

        // Check if user has enough points for the increase
        if (pointsDifference > 0 && user.getPoints() < pointsDifference) {
            throw new RuntimeException("Insufficient points for bet update");
        }

        // Adjust user's points
        user.setPoints(user.getPoints() - pointsDifference);
        userRepository.save(user);

        // Update bet details
        existingBet.setAmount(newAmount);
        existingBet.setNominee(nominee);
        existingBet.setPlacedAt(LocalDateTime.now()); // Update placement time
        
        betRepository.save(existingBet);
    }

    public void deleteBet(Long betId) {
        Bet bet = betRepository.findById(betId)
                .orElseThrow(() -> new RuntimeException("Bet not found"));

        // Verify the category is not closed
        if (!bet.getCategory().isActive()) {
            throw new RuntimeException("Cannot delete bet, category is closed");
        }

        // Refund the user's points
        User user = bet.getUser();
        user.setPoints(user.getPoints() + bet.getAmount());
        userRepository.save(user);

        // Delete the bet
        betRepository.delete(bet);
    }

    public boolean hasExistingBet(Long userId, Long categoryId, Long nomineeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return betRepository.findByUserOrderByPlacedAtDesc(user).stream()
                .anyMatch(bet -> bet.getCategory().getId().equals(categoryId)
                        && bet.getNominee().getId().equals(nomineeId));
    }

    public Long getExistingBetId(Long userId, Long categoryId, Long nomineeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return betRepository.findByUserOrderByPlacedAtDesc(user).stream()
                .filter(bet -> bet.getCategory().getId().equals(categoryId)
                        && bet.getNominee().getId().equals(nomineeId))
                .findFirst()
                .map(Bet::getId)
                .orElse(null);
    }


    public List<Bet> findBetsByCategory(Long categoryId) {
        return betRepository.findByCategoryId(categoryId);
    }

    public Bet saveBet(Bet bet) {
        return betRepository.save(bet);
    }
}
