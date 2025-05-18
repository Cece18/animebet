package com.crunchybet.betapp.dto;

public class PointsUpdateDTO {
    private int points;
    private String reason;

    // Constructor
    public PointsUpdateDTO(int points, String reason) {
        this.points = points;
        this.reason = reason;
    }

    // Getters and setters
    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}