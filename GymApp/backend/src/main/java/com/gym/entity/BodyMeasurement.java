package com.gym.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "body_measurements")
public class BodyMeasurement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @Column(name = "weight", nullable = false)
    private Double weight;
    
    @Column(name = "body_fat")
    private Double bodyFat;
    
    @Column(name = "muscle_mass")
    private Double muscleMass;
    
    @Column(name = "waist")
    private Double waist;
    
    @Column(name = "hip")
    private Double hip;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public Double getWeight() {
        return weight;
    }
    
    public void setWeight(Double weight) {
        this.weight = weight;
    }
    
    public Double getBodyFat() {
        return bodyFat;
    }
    
    public void setBodyFat(Double bodyFat) {
        this.bodyFat = bodyFat;
    }
    
    public Double getMuscleMass() {
        return muscleMass;
    }
    
    public void setMuscleMass(Double muscleMass) {
        this.muscleMass = muscleMass;
    }
    
    public Double getWaist() {
        return waist;
    }
    
    public void setWaist(Double waist) {
        this.waist = waist;
    }
    
    public Double getHip() {
        return hip;
    }
    
    public void setHip(Double hip) {
        this.hip = hip;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
