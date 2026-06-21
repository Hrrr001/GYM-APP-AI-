package com.gym.service;

import com.gym.entity.TrainingPlan;
import com.gym.entity.PlanDetail;
import com.gym.entity.User;
import com.gym.entity.Exercise;
import com.gym.repository.TrainingPlanRepository;
import com.gym.repository.PlanDetailRepository;
import com.gym.repository.ExerciseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TrainingPlanService {
    @Autowired
    private TrainingPlanRepository trainingPlanRepository;
    
    @Autowired
    private PlanDetailRepository planDetailRepository;
    
    @Autowired
    private ExerciseRepository exerciseRepository;
    
    @Autowired
    private AIService aiService;
    
    @Autowired
    private UserService userService;
    
    // 获取用户的所有训练计划
    public List<TrainingPlan> getUserPlans(Long userId) {
        return trainingPlanRepository.findByUserId(userId);
    }
    
    // 根据ID获取训练计划
    public TrainingPlan getPlanById(Long planId) {
        Optional<TrainingPlan> optionalPlan = trainingPlanRepository.findById(planId);
        return optionalPlan.orElse(null);
    }
    
    // 获取计划详情
    public List<PlanDetail> getPlanDetails(Long planId) {
        return planDetailRepository.findByPlanId(planId);
    }
    
    // 获取计划某一周的详情
    public List<PlanDetail> getPlanWeekDetails(Long planId, Integer week) {
        return planDetailRepository.findByPlanIdAndWeek(planId, week);
    }
    
    // 获取计划某一天的详情
    public List<PlanDetail> getPlanDayDetails(Long planId, Integer week, Integer day) {
        return planDetailRepository.findByPlanIdAndWeekAndDay(planId, week, day);
    }
    
    // 创建训练计划
    public TrainingPlan createPlan(User user, String name, String goal, int duration) {
        TrainingPlan plan = new TrainingPlan();
        plan.setUser(user);
        plan.setName(name);
        plan.setGoal(goal);
        plan.setDuration(duration);
        return trainingPlanRepository.save(plan);
    }
    
    // 添加计划详情
    public PlanDetail addPlanDetail(TrainingPlan plan, int week, int day, Exercise exercise, 
                                   int sets, int reps, Double weight, Integer restTime) {
        PlanDetail detail = new PlanDetail();
        detail.setPlan(plan);
        detail.setWeek(week);
        detail.setDay(day);
        detail.setExercise(exercise);
        detail.setSets(sets);
        detail.setReps(reps);
        detail.setWeight(weight);
        detail.setRestTime(restTime);
        return planDetailRepository.save(detail);
    }
    
    // AI生成训练计划
    public TrainingPlan generatePlanWithAI(Long userId, String goal, int duration, String additionalRequirements) {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 调用AI服务生成计划
        String aiResponse = aiService.generateTrainingPlan(
            user.toString(), goal, duration
        );
        
        // 解析AI响应，创建计划
        // 暂时创建一个基本计划
        TrainingPlan plan = createPlan(user, "AI生成计划", goal, duration);
        
        // 添加一些示例动作
        List<Exercise> exercises = exerciseRepository.findByIsCustomFalse();
        if (!exercises.isEmpty()) {
            for (int week = 1; week <= duration; week++) {
                for (int day = 1; day <= 3; day++) {
                    for (int i = 0; i < Math.min(3, exercises.size()); i++) {
                        addPlanDetail(plan, week, day, exercises.get(i), 3, 10, null, 60);
                    }
                }
            }
        }
        
        return plan;
    }
    
    // 更新训练计划
    public TrainingPlan updatePlan(Long planId, String name, String goal, Integer duration) {
        Optional<TrainingPlan> optionalPlan = trainingPlanRepository.findById(planId);
        if (optionalPlan.isEmpty()) {
            throw new RuntimeException("计划不存在");
        }
        
        TrainingPlan plan = optionalPlan.get();
        if (name != null) plan.setName(name);
        if (goal != null) plan.setGoal(goal);
        if (duration != null) plan.setDuration(duration);
        
        return trainingPlanRepository.save(plan);
    }
    
    // 更新计划详情
    public PlanDetail updatePlanDetail(Long detailId, Integer sets, Integer reps, Double weight, Integer restTime) {
        Optional<PlanDetail> optionalDetail = planDetailRepository.findById(detailId);
        if (optionalDetail.isEmpty()) {
            throw new RuntimeException("计划详情不存在");
        }
        
        PlanDetail detail = optionalDetail.get();
        if (sets != null) detail.setSets(sets);
        if (reps != null) detail.setReps(reps);
        if (weight != null) detail.setWeight(weight);
        if (restTime != null) detail.setRestTime(restTime);
        
        return planDetailRepository.save(detail);
    }
    
    // 删除训练计划
    public void deletePlan(Long planId) {
        // 先删除计划详情
        List<PlanDetail> details = planDetailRepository.findByPlanId(planId);
        planDetailRepository.deleteAll(details);
        // 再删除计划
        trainingPlanRepository.deleteById(planId);
    }
    
    // 删除计划详情
    public void deletePlanDetail(Long detailId) {
        planDetailRepository.deleteById(detailId);
    }
}
