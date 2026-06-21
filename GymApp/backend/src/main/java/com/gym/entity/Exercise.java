package com.gym.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exercises")
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "category", nullable = false)
    private String category;
    
    @Column(name = "equipment", nullable = false)
    private String equipment;
    
    @Column(name = "difficulty", nullable = false)
    private String difficulty;
    
    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @Column(name = "instructions", columnDefinition = "TEXT", nullable = false)
    private String instructions;
    
    @Column(name = "tips", columnDefinition = "TEXT")
    private String tips;
    
    @Column(name = "precautions", columnDefinition = "TEXT")
    private String precautions;
    
    @Column(name = "is_custom", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isCustom;
    
    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
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
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getEquipment() {
        return equipment;
    }
    
    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }
    
    public String getDifficulty() {
        return difficulty;
    }
    
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getInstructions() {
        return instructions;
    }
    
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
    
    public String getTips() {
        return tips;
    }
    
    public void setTips(String tips) {
        this.tips = tips;
    }
    
    public String getPrecautions() {
        return precautions;
    }
    
    public void setPrecautions(String precautions) {
        this.precautions = precautions;
    }
    
    public boolean isCustom() {
        return isCustom;
    }
    
    public void setCustom(boolean isCustom) {
        this.isCustom = isCustom;
    }
    
    public User getCreator() {
        return creator;
    }
    
    public void setCreator(User creator) {
        this.creator = creator;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
