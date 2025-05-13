package com.crunchybet.betapp.dto;

import lombok.Data;

@Data
public class PlaceBetRequest {
    private Long categoryId;
    private Long nomineeId;
    private Integer amount;

    public Long getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    public Long getNomineeId() {
        return nomineeId;
    }
    public void setNomineeId(Long nomineeId) {
        this.nomineeId = nomineeId;
    }
    public Integer getAmount() {
        return amount;
    }
    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}
