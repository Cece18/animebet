package com.crunchybet.betapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;
    private Long winnerID;

    private boolean isActive = true;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Nominee> nominees;

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
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public List<Nominee> getNominees() {
        return nominees;
    }
    public void setNominees(List<Nominee> nominees) {
        this.nominees = nominees;
    }
    public void addNominee(Nominee nominee) {
        nominees.add(nominee);
        nominee.setCategory(this);
    }
    public void removeNominee(Nominee nominee) {
        nominees.remove(nominee);
        nominee.setCategory(null);
    }
    public void setNominee(Nominee nominee) {
        this.nominees = List.of(nominee);
        nominee.setCategory(this);
    }
    public Long getWinnerID() {
        return winnerID;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    public boolean isActive() {
        return isActive;
    }
    public void setWinnerID(Long winnerID) {
        this.winnerID = winnerID;
    }
}