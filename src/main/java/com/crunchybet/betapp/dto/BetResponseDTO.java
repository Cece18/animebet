package com.crunchybet.betapp.dto;

import com.crunchybet.betapp.model.enums.BetStatus;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Bet response.
 */
public class BetResponseDTO {
    private Long id;
    private String category;
    private String nominee;
    private Integer amount;
    private LocalDateTime placedAt;
    private BetStatus status = BetStatus.PENDING;


    public BetResponseDTO() {}

    public BetResponseDTO(Long id,String category, String nominee, Integer amount) {
        this.id = id;
        this.category = category;
        this.nominee = nominee;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getNominee() {
        return nominee;
    }

    public void setNominee(String nominee) {
        this.nominee = nominee;
    }

    public Integer getPointsBet() { // Changed from getAmount
        return amount;
    }

    public void setStatus(BetStatus status) {
        this.status = status;
    }
    public BetStatus getStatus() {
        return status;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public LocalDateTime getBetDate() { // Changed from getPlacedAt
        return placedAt;
    }

    public void setPlacedAt(LocalDateTime placedAt) {
        this.placedAt = placedAt;
    }


}
