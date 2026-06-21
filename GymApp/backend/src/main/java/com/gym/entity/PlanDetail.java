package com.gym.entity;

import javax.persistence.*;

@Entity
@Table(name = "plan_details")
public class PlanDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private TrainingPlan plan;
    
    @Column(name = "week", nullable = false)
    private Integer week;
    
    @Column(name = "day", nullable = false)
    private Integer day;
    
    @ManyToOne
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;
    
    @Column(name = "sets", nullable = false)
    private Integer sets;
    
    @Column(name = "reps", nullable = false)
    private Integer reps;
    
    @Column(name = "weight")
    private Double weight;
    
    @Column(name = "rest_time")
    private Integer restTime;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public TrainingPlan getPlan() {
        return plan;
    }
    
    public void setPlan(TrainingPlan plan) {
        this.plan = plan;
    }
    
    public Integer getWeek() {
        return week;
    }
    
    public void setWeek(Integer week) {
        this.week = week;
    }
    
    public Integer getDay() {
        return day;
    }
    
    public void setDay(Integer day) {
        this.day = day;
    }
    
    public Exercise getExercise() {
        return exercise;
    }
    
    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }
    
    public Integer getSets() {
        return sets;
    }
    
    public void setSets(Integer sets) {
        this.sets = sets;
    }
    
    public Integer getReps() {
        return reps;
    }
    
    public void setReps(Integer reps) {
        this.reps = reps;
    }
    
    public Double getWeight() {
        return weight;
    }
    
    public void setWeight(Double weight) {
        this.weight = weight;
    }
    
    public Integer getRestTime() {
        return restTime;
    }
    
    public void setRestTime(Integer restTime) {
        this.restTime = restTime;
    }
}
