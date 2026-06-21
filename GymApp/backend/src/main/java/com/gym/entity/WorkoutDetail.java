package com.gym.entity;

import javax.persistence.*;

@Entity
@Table(name = "workout_details")
public class WorkoutDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "record_id", nullable = false)
    private WorkoutRecord record;
    
    @ManyToOne
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;
    
    @Column(name = "sets", nullable = false)
    private Integer sets;
    
    @Column(name = "reps", nullable = false)
    private Integer reps;
    
    @Column(name = "weight")
    private Double weight;
    
    @Column(name = "completed", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean completed;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public WorkoutRecord getRecord() {
        return record;
    }
    
    public void setRecord(WorkoutRecord record) {
        this.record = record;
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
    
    public boolean isCompleted() {
        return completed;
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
