package com.gym.repository;

import com.gym.entity.FoodRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDate;

public interface FoodRecordRepository extends JpaRepository<FoodRecord, Long> {
    List<FoodRecord> findByUserId(Long userId);
    List<FoodRecord> findByUserIdAndDate(Long userId, LocalDate date);
    List<FoodRecord> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    List<FoodRecord> findByUserIdAndMealType(Long userId, String mealType);
}
