package com.gym.repository;

import com.gym.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    List<Exercise> findByCategory(String category);
    List<Exercise> findByEquipment(String equipment);
    List<Exercise> findByDifficulty(String difficulty);
    List<Exercise> findByIsCustomFalse();
    List<Exercise> findByCreatorId(Long creatorId);
    long countByIsCustomFalse();
}
