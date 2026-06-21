package com.gym.controller;

import com.gym.entity.FoodRecord;
import com.gym.entity.BodyMeasurement;
import com.gym.entity.User;
import com.gym.service.NutritionService;
import com.gym.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/nutrition")
public class NutritionController {
    @Autowired
    private NutritionService nutritionService;
    
    @Autowired
    private UserService userService;
    
    // 获取用户的所有饮食记录
    @GetMapping("/food/user/{userId}")
    public ResponseEntity<?> getUserFoodRecords(@PathVariable Long userId) {
        try {
            List<FoodRecord> records = nutritionService.getUserFoodRecords(userId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 获取用户某天的饮食记录
    @GetMapping("/food/user/{userId}/date/{date}")
    public ResponseEntity<?> getUserFoodRecordsByDate(@PathVariable Long userId, @PathVariable String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            List<FoodRecord> records = nutritionService.getUserFoodRecordsByDate(userId, localDate);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 获取用户某时间段的饮食记录
    @GetMapping("/food/user/{userId}/date-range")
    public ResponseEntity<?> getUserFoodRecordsByDateRange(
            @PathVariable Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            List<FoodRecord> records = nutritionService.getUserFoodRecordsByDateRange(userId, start, end);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 创建饮食记录
    @PostMapping("/food/create")
    public ResponseEntity<?> createFoodRecord(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            LocalDate date = LocalDate.parse(request.get("date").toString());
            String mealType = (String) request.get("mealType");
            String foodName = (String) request.get("foodName");
            double calories = Double.parseDouble(request.get("calories").toString());
            Double protein = request.get("protein") != null ? Double.parseDouble(request.get("protein").toString()) : null;
            Double carbs = request.get("carbs") != null ? Double.parseDouble(request.get("carbs").toString()) : null;
            Double fat = request.get("fat") != null ? Double.parseDouble(request.get("fat").toString()) : null;
            
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "用户不存在"));
            }
            
            FoodRecord record = nutritionService.createFoodRecord(user, date, mealType, foodName, calories, protein, carbs, fat);
            return ResponseEntity.ok(Map.of("message", "饮食记录创建成功", "recordId", record.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // AI辅助创建饮食记录
    @PostMapping("/food/ai/create")
    public ResponseEntity<?> createFoodRecordWithAI(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            LocalDate date = LocalDate.parse(request.get("date").toString());
            String mealType = (String) request.get("mealType");
            String description = (String) request.get("description");
            
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "用户不存在"));
            }
            
            // 检查是否与饮食相关
            if (!nutritionService.isDietRelated(description)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "饮食计划生成失败：请输入与饮食相关的描述"));
            }
            
            FoodRecord record = nutritionService.createFoodRecordWithAI(user, date, mealType, description);
            return ResponseEntity.ok(Map.of("message", "饮食记录创建成功", "recordId", record.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 获取AI饮食建议
    @GetMapping("/food/ai/suggestion/{userId}")
    public ResponseEntity<?> getAIDietSuggestion(@PathVariable Long userId) {
        try {
            String suggestion = nutritionService.getAIDietSuggestion(userId);
            return ResponseEntity.ok(Map.of("suggestion", suggestion));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 删除饮食记录
    @DeleteMapping("/food/{recordId}")
    public ResponseEntity<?> deleteFoodRecord(@PathVariable Long recordId) {
        try {
            nutritionService.deleteFoodRecord(recordId);
            return ResponseEntity.ok(Map.of("message", "饮食记录删除成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 获取用户的所有身体数据记录
    @GetMapping("/body/user/{userId}")
    public ResponseEntity<?> getUserBodyMeasurements(@PathVariable Long userId) {
        try {
            List<BodyMeasurement> measurements = nutritionService.getUserBodyMeasurements(userId);
            return ResponseEntity.ok(measurements);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 获取用户某时间段的身体数据记录
    @GetMapping("/body/user/{userId}/date-range")
    public ResponseEntity<?> getUserBodyMeasurementsByDateRange(
            @PathVariable Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            List<BodyMeasurement> measurements = nutritionService.getUserBodyMeasurementsByDateRange(userId, start, end);
            return ResponseEntity.ok(measurements);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 创建身体数据记录
    @PostMapping("/body/create")
    public ResponseEntity<?> createBodyMeasurement(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            LocalDate date = LocalDate.parse(request.get("date").toString());
            double weight = Double.parseDouble(request.get("weight").toString());
            Double bodyFat = request.get("bodyFat") != null ? Double.parseDouble(request.get("bodyFat").toString()) : null;
            Double muscleMass = request.get("muscleMass") != null ? Double.parseDouble(request.get("muscleMass").toString()) : null;
            Double waist = request.get("waist") != null ? Double.parseDouble(request.get("waist").toString()) : null;
            Double hip = request.get("hip") != null ? Double.parseDouble(request.get("hip").toString()) : null;
            
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "用户不存在"));
            }
            
            BodyMeasurement measurement = nutritionService.createBodyMeasurement(user, date, weight, bodyFat, muscleMass, waist, hip);
            return ResponseEntity.ok(Map.of("message", "身体数据记录创建成功", "measurementId", measurement.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 删除身体数据记录
    @DeleteMapping("/body/{measurementId}")
    public ResponseEntity<?> deleteBodyMeasurement(@PathVariable Long measurementId) {
        try {
            nutritionService.deleteBodyMeasurement(measurementId);
            return ResponseEntity.ok(Map.of("message", "身体数据记录删除成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
}
