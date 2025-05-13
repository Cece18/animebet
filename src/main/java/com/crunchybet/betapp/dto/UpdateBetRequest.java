package com.crunchybet.betapp.dto;


import lombok.Data;

@Data
public class UpdateBetRequest {
    private Long nomineeId;
    private Integer newAmount;

    public Long getNomineeId() {
        return nomineeId;
    }
    public void setNomineeId(Long nomineeId) {
        this.nomineeId = nomineeId;
    }
    public Integer getNewAmount() {
        return newAmount;
    }
    public void setNewAmount(Integer newAmount) {
        this.newAmount = newAmount;
    }
}
