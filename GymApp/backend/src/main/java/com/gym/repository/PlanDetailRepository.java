package com.gym.repository;

import com.gym.entity.PlanDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlanDetailRepository extends JpaRepository<PlanDetail, Long> {
    List<PlanDetail> findByPlanId(Long planId);
    List<PlanDetail> findByPlanIdAndWeek(Long planId, Integer week);
    List<PlanDetail> findByPlanIdAndWeekAndDay(Long planId, Integer week, Integer day);
}
