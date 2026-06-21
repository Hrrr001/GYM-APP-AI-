package com.gym.repository;

import com.gym.entity.TrainingPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrainingPlanRepository extends JpaRepository<TrainingPlan, Long> {
    List<TrainingPlan> findByUserId(Long userId);
}
