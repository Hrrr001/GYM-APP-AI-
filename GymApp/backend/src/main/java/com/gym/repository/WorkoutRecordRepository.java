package com.gym.repository;

import com.gym.entity.WorkoutRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDate;

public interface WorkoutRecordRepository extends JpaRepository<WorkoutRecord, Long> {
    List<WorkoutRecord> findByUserId(Long userId);
    List<WorkoutRecord> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    List<WorkoutRecord> findByPlanId(Long planId);
}
