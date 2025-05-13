package com.crunchybet.betapp.service;

import com.crunchybet.betapp.dto.BetResponseDTO;
import com.crunchybet.betapp.model.Bet;
import com.crunchybet.betapp.model.Category;
import com.crunchybet.betapp.model.Nominee;
import com.crunchybet.betapp.model.User;
import com.crunchybet.betapp.repository.BetRepository;
import com.crunchybet.betapp.repository.CategoryRepository;
import com.crunchybet.betapp.repository.NomineeRepository;
import com.crunchybet.betapp.repository.UserRepository;
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

    //Method to get bets of a user
    public List<BetResponseDTO> getUserBets(Long userId) {
        List<Bet> bets = betRepository.findByUserId(userId);

        return bets.stream()
                .map(bet -> {
                    BetResponseDTO dto = new BetResponseDTO(
                            bet.getId(),
                            bet.getCategory().getName(),
                            bet.getNominee().getName(),
                            bet.getAmount()
                    );
                    dto.setPlacedAt(bet.getPlacedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public void placeBet(Long categoryId, Long nomineeId, Long userId, Integer amount) {
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
        // Deduct the amount from user's points
        user.setPoints(user.getPoints() - amount);
        userRepository.save(user);

        // Create new bet with all required fields
        Bet bet = new Bet();
        bet.setCategory(category);
        bet.setNominee(nominee);
        bet.setAmount(amount);
        bet.setIsWinner(false);
        bet.setWinningAmount(0.0);
        bet.setPlacedAt(LocalDateTime.now());
        bet.setCreatedAt(LocalDateTime.now());
        bet.setUser(user);

        betRepository.save(bet);
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
}
