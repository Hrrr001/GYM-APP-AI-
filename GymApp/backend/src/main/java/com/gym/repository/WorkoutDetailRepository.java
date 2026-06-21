package com.gym.repository;

import com.gym.entity.WorkoutDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkoutDetailRepository extends JpaRepository<WorkoutDetail, Long> {
    List<WorkoutDetail> findByRecordId(Long recordId);
}
