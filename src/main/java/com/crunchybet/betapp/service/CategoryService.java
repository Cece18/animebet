package com.crunchybet.betapp.service;

import com.crunchybet.betapp.controller.WebSocketNotificationController;
import com.crunchybet.betapp.dto.CategoryDTO;
import com.crunchybet.betapp.dto.CategoryOnlyDTO;
import com.crunchybet.betapp.dto.NomineeDTO;
import com.crunchybet.betapp.model.Bet;
import com.crunchybet.betapp.model.Category;
import com.crunchybet.betapp.model.Nominee;
import com.crunchybet.betapp.model.User;
import com.crunchybet.betapp.model.enums.BetStatus;
import com.crunchybet.betapp.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BetService betService;

    @Autowired
    private WebSocketNotificationController webSocketController;


    //just get categories no nominees
    public List<CategoryOnlyDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(category -> {
                    CategoryOnlyDTO categoryDTO = new CategoryOnlyDTO();
                    categoryDTO.setId(category.getId());
                    categoryDTO.setName(category.getName());
                    categoryDTO.setActive(category.isActive());
                    return categoryDTO;
                })
                .collect(Collectors.toList());
    }

    public List<NomineeDTO> getNomineesByCategoryName(String categoryName) {
        Category category = categoryRepository.findByNameOrderByNameAsc(categoryName).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryName));

        return category.getNominees().stream()
                .map(nominee -> {
                    NomineeDTO nomineeDTO = new NomineeDTO();
                    nomineeDTO.setId(nominee.getId());
                    nomineeDTO.setName(nominee.getName());
                    nomineeDTO.setImageUrl(nominee.getImageUrl());
                    nomineeDTO.setMultiplier(nominee.getMultiplier());
                    nomineeDTO.setCategoryId(category.getId());
                    return nomineeDTO;
                })
                .collect(Collectors.toList());
    }

    public List<CategoryDTO> findTopNWithNominees(int limit) {
        List<Category> categories = categoryRepository.findTopNOrderByIdAsc(PageRequest.of(0, limit));
        return categories.stream()
                .map(category -> {
                    CategoryDTO categoryDTO = new CategoryDTO();
                    categoryDTO.setId(category.getId());
                    categoryDTO.setName(category.getName());
                    categoryDTO.setActive(category.isActive());
                    categoryDTO.setDescription(category.getDescription());
                    categoryDTO.setNominees(category.getNominees().stream()
                            .map(nominee -> {
                                NomineeDTO nomineeDTO = new NomineeDTO();
                                nomineeDTO.setId(nominee.getId());
                                nomineeDTO.setName(nominee.getName());
                                nomineeDTO.setImageUrl(nominee.getImageUrl());
                                nomineeDTO.setMultiplier(nominee.getMultiplier());
                                return nomineeDTO;
                            })
                            .collect(Collectors.toList()));
                    return categoryDTO;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void setWinnerAndProcessBets(Long categoryId, Long nomineeId) {
        // 1. Validate and set winner
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Nominee winner = category.getNominees().stream()
                .filter(n -> n.getId().equals(nomineeId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Nominee not found in this category"));

        // Check if the category is already closed + close it if not
        if (category.isActive()) {
            category.setActive(false);
        }
        // 2. Set winner ID
        category.setWinnerID(nomineeId);
        categoryRepository.save(category);

        // 3. Process all bets for this category
        processBetsForCategory(category, winner);
    }

    private void processBetsForCategory(Category category, Nominee winner) {
        List<Bet> categoryBets = betService.findBetsByCategory(category.getId());

        for (Bet bet : categoryBets) {
            if (bet.getNominee().getId().equals(winner.getId())) {
                // Winner bet
                bet.setStatus(BetStatus.WINNER);
                double winnings = bet.getAmount() * bet.getNominee().getMultiplier();
                bet.setWinningAmount(winnings);

                // Update user points
                User user = bet.getUser();
                user.setPoints(user.getPoints() + (int) winnings);

                //Create Notification
                notificationService.createBetResultNotification(user, category, true, winnings);
                webSocketController.sendPointsUpdate(
                        user.getUsername(),
                        user.getPoints(),
                        "Won bet: +" + (int)winnings + " points"
                );

            } else {
                // Losing bet
                bet.setStatus(BetStatus.LOSER);
                bet.setWinningAmount(0.0);
                notificationService.createBetResultNotification(bet.getUser(), category, false, 0);

            }
            betService.saveBet(bet);
        }
    }

    public void closeAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        for (Category category : categories) {
            category.setActive(false);
            categoryRepository.save(category);
        }
    }
}
