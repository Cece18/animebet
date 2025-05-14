package com.crunchybet.betapp.model;

import com.crunchybet.betapp.model.enums.BetStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "bets")
public class Bet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer amount;

    @Enumerated(EnumType.STRING)
    private BetStatus status = BetStatus.PENDING;

    private Double winningAmount;

    private LocalDateTime placedAt;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "nominee_id", nullable = false)
    private Nominee nominee;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Integer getAmount() {
        return amount;
    }
    public void setAmount(Integer amount) {
        this.amount = amount;
    }
    public BetStatus getStatus() {
        return status;
    }

    public void setStatus(BetStatus status) {
        this.status = status;
    }
    public void setWinningAmount(Double winningAmount) {
        this.winningAmount = winningAmount;
    }
    public LocalDateTime getPlacedAt() {
        return placedAt;
    }
    public void setPlacedAt(LocalDateTime placedAt) {
        this.placedAt = placedAt;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public Nominee getNominee() {
        return nominee;
    }
    public void setNominee(Nominee nominee) {
        this.nominee = nominee;
    }
    public Category getCategory() {
        return category;
    }
    public void setCategory(Category category) {
        this.category = category;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
}