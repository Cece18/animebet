package com.crunchybet.betapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//@Data
//@AllArgsConstructor
//@NoArgsConstructor
public class BetResponseDTO {
    private Long id;
    private String category;
    private String nominee;
    private Integer amount;
    private LocalDateTime placedAt;


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
