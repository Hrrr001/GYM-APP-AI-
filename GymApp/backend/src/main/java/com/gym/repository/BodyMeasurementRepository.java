package com.gym.repository;

import com.gym.entity.BodyMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDate;

public interface BodyMeasurementRepository extends JpaRepository<BodyMeasurement, Long> {
    List<BodyMeasurement> findByUserId(Long userId);
    List<BodyMeasurement> findByUserIdAndDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
