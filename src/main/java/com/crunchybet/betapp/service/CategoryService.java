package com.crunchybet.betapp.service;

import com.crunchybet.betapp.dto.CategoryDTO;
import com.crunchybet.betapp.dto.CategoryOnlyDTO;
import com.crunchybet.betapp.dto.NomineeDTO;
import com.crunchybet.betapp.model.Bet;
import com.crunchybet.betapp.model.Category;
import com.crunchybet.betapp.model.Nominee;
import com.crunchybet.betapp.model.User;
import com.crunchybet.betapp.model.enums.BetStatus;
import com.crunchybet.betapp.repository.BetRepository;
import com.crunchybet.betapp.repository.CategoryRepository;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;


import java.util.Comparator;
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
    private SseService sseService;

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);
    @Autowired
    private BetRepository betRepository;


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


    @Cacheable(value = "nomineesByCategory", key = "#categoryName")
    @Transactional(readOnly = true)
    public List<NomineeDTO> getNomineesByCategoryName(String categoryName) {
        Category category = categoryRepository.findByNameWithNominees(categoryName).stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryName));

        return category.getNominees().stream()
                .sorted(Comparator.comparing(Nominee::getId))
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

    @Cacheable(value = "topCategoriesWithNominees", key = "#limit")
    @Transactional(readOnly = true)
    public List<CategoryDTO> findTopNWithNominees(int limit) {
        List<Category> categories = categoryRepository.findTopNOrderByIdAsc(PageRequest.of(0, limit));
        return categories.stream()
                .map(category -> {
                    CategoryDTO dto = new CategoryDTO();
                    dto.setId(category.getId());
                    dto.setName(category.getName());
                    dto.setDescription(category.getDescription());
                    dto.setActive(category.isActive());
                    dto.setNominees(category.getNominees().stream()
                            .map(nominee -> {
                                NomineeDTO nomineeDTO = new NomineeDTO();
                                nomineeDTO.setId(nominee.getId());
                                nomineeDTO.setName(nominee.getName());
                                nomineeDTO.setImageUrl(nominee.getImageUrl());
                                nomineeDTO.setMultiplier(nominee.getMultiplier());
                                return nomineeDTO;
                            })
                            .collect(Collectors.toList()));
                    return dto;
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

        // Check if category already has a winner
        if (category.getWinnerID() != null) {
            throw new RuntimeException("Category already has a winner");
        }

        // 2. Set winner ID
        category.setWinnerID(nomineeId);
        categoryRepository.save(category);

        // 3. Process all bets for this category
        processBetsForCategory(category, winner);
    }


    public void processBetsForCategory(Category category, Nominee winner) {
        List<Bet> categoryBets = betService.findBetsByCategory(category.getId());
        logger.info("Processing {} bets for category {}", categoryBets.size(), category.getName());


        for (Bet bet : categoryBets) {
            if (bet.getNominee().getId().equals(winner.getId())) {
                // Winner bet
                bet.setStatus(BetStatus.WINNER);
                double winnings = bet.getAmount() * bet.getNominee().getMultiplier();
                bet.setWinningAmount(winnings);

                // Update user points
                User user = bet.getUser();

                logger.info("Processing winning bet for user {}: amount={}, multiplier={}, winnings={}",
                        user.getUsername(), bet.getAmount(), bet.getNominee().getMultiplier(), winnings);

                user.setPoints(user.getPoints() + (int) winnings);

                //Create Notification
                notificationService.createBetResultNotification(user, category, true, winnings);

                logger.info("Attempting to send SSE update to {}", user.getUsername());

                sseService.sendPointsUpdate(user.getEmail(), user.getPoints(),
                        "Won bet: +" + (int)winnings + " points");


            } else {
                // Losing bet
                bet.setStatus(BetStatus.LOSER);
                bet.setWinningAmount(0.0);
                notificationService.createBetResultNotification(bet.getUser(), category, false, 0);

            }
            betRepository.save(bet);
        }
    }

    @Transactional
    public void closeAllCategories() {
        List<Category> categories = categoryRepository.findAll();

        categories.forEach(c -> c.setActive(false));
        categoryRepository.saveAll(categories);
    }

    @Transactional
    public void openAllCategories() {
        List<Category> categories = categoryRepository.findAll();

        // Only activate categories that don't have a winner
        List<Category> toActivate = categories.stream()
                .filter(c -> c.getWinnerID() == null)
                .peek(c -> c.setActive(true))
                .toList();

        categoryRepository.saveAll(toActivate);
    }


}
