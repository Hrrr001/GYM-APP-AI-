package com.gym.controller;

import com.gym.entity.TrainingPlan;
import com.gym.entity.PlanDetail;
import com.gym.entity.User;
import com.gym.entity.Exercise;
import com.gym.service.TrainingPlanService;
import com.gym.service.UserService;
import com.gym.service.ExerciseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plans")
public class TrainingPlanController {
    @Autowired
    private TrainingPlanService trainingPlanService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ExerciseService exerciseService;
    
    // 获取用户的所有训练计划
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserPlans(@PathVariable Long userId) {
        try {
            List<TrainingPlan> plans = trainingPlanService.getUserPlans(userId);
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 根据ID获取训练计划
    @GetMapping("/{planId}")
    public ResponseEntity<?> getPlanById(@PathVariable Long planId) {
        try {
            TrainingPlan plan = trainingPlanService.getPlanById(planId);
            if (plan == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "计划不存在"));
            }
            return ResponseEntity.ok(plan);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 获取计划详情
    @GetMapping("/{planId}/details")
    public ResponseEntity<?> getPlanDetails(@PathVariable Long planId) {
        try {
            List<PlanDetail> details = trainingPlanService.getPlanDetails(planId);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 获取计划某一周的详情
    @GetMapping("/{planId}/week/{week}")
    public ResponseEntity<?> getPlanWeekDetails(@PathVariable Long planId, @PathVariable Integer week) {
        try {
            List<PlanDetail> details = trainingPlanService.getPlanWeekDetails(planId, week);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 获取计划某一天的详情
    @GetMapping("/{planId}/week/{week}/day/{day}")
    public ResponseEntity<?> getPlanDayDetails(@PathVariable Long planId, @PathVariable Integer week, @PathVariable Integer day) {
        try {
            List<PlanDetail> details = trainingPlanService.getPlanDayDetails(planId, week, day);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 创建训练计划
    @PostMapping("/create")
    public ResponseEntity<?> createPlan(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            String name = (String) request.get("name");
            String goal = (String) request.get("goal");
            int duration = Integer.parseInt(request.get("duration").toString());
            
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "用户不存在"));
            }
            
            TrainingPlan plan = trainingPlanService.createPlan(user, name, goal, duration);
            return ResponseEntity.ok(Map.of("message", "计划创建成功", "planId", plan.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 添加计划详情
    @PostMapping("/{planId}/details")
    public ResponseEntity<?> addPlanDetail(@PathVariable Long planId, @RequestBody Map<String, Object> request) {
        try {
            int week = Integer.parseInt(request.get("week").toString());
            int day = Integer.parseInt(request.get("day").toString());
            Long exerciseId = Long.parseLong(request.get("exerciseId").toString());
            int sets = Integer.parseInt(request.get("sets").toString());
            int reps = Integer.parseInt(request.get("reps").toString());
            Double weight = request.get("weight") != null ? Double.parseDouble(request.get("weight").toString()) : null;
            Integer restTime = request.get("restTime") != null ? Integer.parseInt(request.get("restTime").toString()) : null;
            
            TrainingPlan plan = trainingPlanService.getPlanById(planId);
            if (plan == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "计划不存在"));
            }
            
            Exercise exercise = exerciseService.getExerciseById(exerciseId);
            if (exercise == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "动作不存在"));
            }
            
            PlanDetail detail = trainingPlanService.addPlanDetail(plan, week, day, exercise, sets, reps, weight, restTime);
            return ResponseEntity.ok(Map.of("message", "详情添加成功", "detailId", detail.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // AI生成训练计划
    @PostMapping("/ai/generate")
    public ResponseEntity<?> generatePlanWithAI(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            String goal = (String) request.get("goal");
            int duration = Integer.parseInt(request.get("duration").toString());
            String additionalRequirements = (String) request.get("additionalRequirements");
            
            TrainingPlan plan = trainingPlanService.generatePlanWithAI(userId, goal, duration, additionalRequirements);
            return ResponseEntity.ok(Map.of("message", "计划生成成功", "planId", plan.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 更新训练计划
    @PutMapping("/{planId}")
    public ResponseEntity<?> updatePlan(@PathVariable Long planId, @RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String goal = (String) request.get("goal");
            Integer duration = request.get("duration") != null ? Integer.parseInt(request.get("duration").toString()) : null;
            
            TrainingPlan plan = trainingPlanService.updatePlan(planId, name, goal, duration);
            return ResponseEntity.ok(Map.of("message", "计划更新成功", "planId", plan.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 更新计划详情
    @PutMapping("/details/{detailId}")
    public ResponseEntity<?> updatePlanDetail(@PathVariable Long detailId, @RequestBody Map<String, Object> request) {
        try {
            Integer sets = request.get("sets") != null ? Integer.parseInt(request.get("sets").toString()) : null;
            Integer reps = request.get("reps") != null ? Integer.parseInt(request.get("reps").toString()) : null;
            Double weight = request.get("weight") != null ? Double.parseDouble(request.get("weight").toString()) : null;
            Integer restTime = request.get("restTime") != null ? Integer.parseInt(request.get("restTime").toString()) : null;
            
            PlanDetail detail = trainingPlanService.updatePlanDetail(detailId, sets, reps, weight, restTime);
            return ResponseEntity.ok(Map.of("message", "详情更新成功", "detailId", detail.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 删除训练计划
    @DeleteMapping("/{planId}")
    public ResponseEntity<?> deletePlan(@PathVariable Long planId) {
        try {
            trainingPlanService.deletePlan(planId);
            return ResponseEntity.ok(Map.of("message", "计划删除成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 删除计划详情
    @DeleteMapping("/details/{detailId}")
    public ResponseEntity<?> deletePlanDetail(@PathVariable Long detailId) {
        try {
            trainingPlanService.deletePlanDetail(detailId);
            return ResponseEntity.ok(Map.of("message", "详情删除成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
}
