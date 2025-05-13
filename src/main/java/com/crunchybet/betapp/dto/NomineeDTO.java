package com.crunchybet.betapp.dto;

public class NomineeDTO {
    private Long id;
    private String name;
    private String imageUrl;
    private Double multiplier;
    private Integer totalBets;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(Double odds) {
        this.multiplier = odds;
    }


    public Integer getTotalBets() {
        return totalBets;
    }

    public void setTotalBets(Integer totalBets) {
        this.totalBets = totalBets;
    }
}
