package com.gym.controller;

import com.gym.entity.WorkoutRecord;
import com.gym.entity.WorkoutDetail;
import com.gym.entity.User;
import com.gym.service.WorkoutService;
import com.gym.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {
    @Autowired
    private WorkoutService workoutService;
    
    @Autowired
    private UserService userService;
    
    // 获取用户的所有训练记录
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserRecords(@PathVariable Long userId) {
        try {
            List<WorkoutRecord> records = workoutService.getUserRecords(userId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 根据日期范围获取用户的训练记录
    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<?> getUserRecordsByDateRange(
            @PathVariable Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            List<WorkoutRecord> records = workoutService.getUserRecordsByDateRange(userId, start, end);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 根据计划获取训练记录
    @GetMapping("/plan/{planId}")
    public ResponseEntity<?> getRecordsByPlan(@PathVariable Long planId) {
        try {
            List<WorkoutRecord> records = workoutService.getRecordsByPlan(planId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 根据ID获取训练记录
    @GetMapping("/{recordId}")
    public ResponseEntity<?> getRecordById(@PathVariable Long recordId) {
        try {
            WorkoutRecord record = workoutService.getRecordById(recordId);
            if (record == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "训练记录不存在"));
            }
            return ResponseEntity.ok(record);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 获取训练记录详情
    @GetMapping("/{recordId}/details")
    public ResponseEntity<?> getRecordDetails(@PathVariable Long recordId) {
        try {
            List<WorkoutDetail> details = workoutService.getRecordDetails(recordId);
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 创建训练记录
    @PostMapping("/create")
    public ResponseEntity<?> createRecord(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            Long planId = request.get("planId") != null ? Long.parseLong(request.get("planId").toString()) : null;
            LocalDate date = LocalDate.parse(request.get("date").toString());
            int duration = Integer.parseInt(request.get("duration").toString());
            String feeling = (String) request.get("feeling");
            String notes = (String) request.get("notes");
            
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "用户不存在"));
            }
            
            WorkoutRecord record = workoutService.createRecord(user, planId, date, duration, feeling, notes);
            return ResponseEntity.ok(Map.of("message", "训练记录创建成功", "recordId", record.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 添加训练记录详情
    @PostMapping("/{recordId}/details")
    public ResponseEntity<?> addRecordDetail(@PathVariable Long recordId, @RequestBody Map<String, Object> request) {
        try {
            Long exerciseId = Long.parseLong(request.get("exerciseId").toString());
            int sets = Integer.parseInt(request.get("sets").toString());
            int reps = Integer.parseInt(request.get("reps").toString());
            Double weight = request.get("weight") != null ? Double.parseDouble(request.get("weight").toString()) : null;
            boolean completed = request.get("completed") != null ? Boolean.parseBoolean(request.get("completed").toString()) : true;
            
            WorkoutRecord record = workoutService.getRecordById(recordId);
            if (record == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "训练记录不存在"));
            }
            
            WorkoutDetail detail = workoutService.addRecordDetail(record, exerciseId, sets, reps, weight, completed);
            return ResponseEntity.ok(Map.of("message", "训练记录详情添加成功", "detailId", detail.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 获取AI训练反馈
    @GetMapping("/{recordId}/ai-feedback")
    public ResponseEntity<?> getAIFeedback(@PathVariable Long recordId) {
        try {
            String feedback = workoutService.getAIFeedback(recordId);
            return ResponseEntity.ok(Map.of("feedback", feedback));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 更新训练记录
    @PutMapping("/{recordId}")
    public ResponseEntity<?> updateRecord(@PathVariable Long recordId, @RequestBody Map<String, Object> request) {
        try {
            Integer duration = request.get("duration") != null ? Integer.parseInt(request.get("duration").toString()) : null;
            String feeling = (String) request.get("feeling");
            String notes = (String) request.get("notes");
            
            WorkoutRecord record = workoutService.updateRecord(recordId, duration, feeling, notes);
            return ResponseEntity.ok(Map.of("message", "训练记录更新成功", "recordId", record.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 更新训练记录详情
    @PutMapping("/details/{detailId}")
    public ResponseEntity<?> updateRecordDetail(@PathVariable Long detailId, @RequestBody Map<String, Object> request) {
        try {
            Integer sets = request.get("sets") != null ? Integer.parseInt(request.get("sets").toString()) : null;
            Integer reps = request.get("reps") != null ? Integer.parseInt(request.get("reps").toString()) : null;
            Double weight = request.get("weight") != null ? Double.parseDouble(request.get("weight").toString()) : null;
            Boolean completed = request.get("completed") != null ? Boolean.parseBoolean(request.get("completed").toString()) : null;
            
            WorkoutDetail detail = workoutService.updateRecordDetail(detailId, sets, reps, weight, completed);
            return ResponseEntity.ok(Map.of("message", "训练记录详情更新成功", "detailId", detail.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 删除训练记录
    @DeleteMapping("/{recordId}")
    public ResponseEntity<?> deleteRecord(@PathVariable Long recordId) {
        try {
            workoutService.deleteRecord(recordId);
            return ResponseEntity.ok(Map.of("message", "训练记录删除成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 删除训练记录详情
    @DeleteMapping("/details/{detailId}")
    public ResponseEntity<?> deleteRecordDetail(@PathVariable Long detailId) {
        try {
            workoutService.deleteRecordDetail(detailId);
            return ResponseEntity.ok(Map.of("message", "训练记录详情删除成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
}
