package com.gym.controller;

import com.gym.entity.Exercise;
import com.gym.entity.User;
import com.gym.service.ExerciseService;
import com.gym.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {
    @Autowired
    private ExerciseService exerciseService;
    
    @Autowired
    private UserService userService;
    
    // 获取所有标准动作
    @GetMapping("/standard")
    public ResponseEntity<?> getStandardExercises() {
        try {
            List<Exercise> exercises = exerciseService.getStandardExercises();
            return ResponseEntity.ok(exercises);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 根据类别获取动作
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getExercisesByCategory(@PathVariable String category) {
        try {
            List<Exercise> exercises = exerciseService.getExercisesByCategory(category);
            return ResponseEntity.ok(exercises);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 根据器械获取动作
    @GetMapping("/equipment/{equipment}")
    public ResponseEntity<?> getExercisesByEquipment(@PathVariable String equipment) {
        try {
            List<Exercise> exercises = exerciseService.getExercisesByEquipment(equipment);
            return ResponseEntity.ok(exercises);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 根据难度获取动作
    @GetMapping("/difficulty/{difficulty}")
    public ResponseEntity<?> getExercisesByDifficulty(@PathVariable String difficulty) {
        try {
            List<Exercise> exercises = exerciseService.getExercisesByDifficulty(difficulty);
            return ResponseEntity.ok(exercises);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 获取用户自定义动作
    @GetMapping("/custom/{userId}")
    public ResponseEntity<?> getCustomExercises(@PathVariable Long userId) {
        try {
            List<Exercise> exercises = exerciseService.getCustomExercises(userId);
            return ResponseEntity.ok(exercises);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 根据ID获取动作
    @GetMapping("/{id}")
    public ResponseEntity<?> getExerciseById(@PathVariable Long id) {
        try {
            Exercise exercise = exerciseService.getExerciseById(id);
            if (exercise == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "动作不存在"));
            }
            return ResponseEntity.ok(exercise);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 创建自定义动作
    @PostMapping("/custom")
    public ResponseEntity<?> createCustomExercise(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            String name = (String) request.get("name");
            String category = (String) request.get("category");
            String equipment = (String) request.get("equipment");
            String difficulty = (String) request.get("difficulty");
            String description = (String) request.get("description");
            String instructions = (String) request.get("instructions");
            String tips = (String) request.get("tips");
            String precautions = (String) request.get("precautions");
            
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "用户不存在"));
            }
            
            Exercise exercise = exerciseService.createCustomExercise(user, name, category, equipment, 
                                                                difficulty, description, instructions, 
                                                                tips, precautions);
            return ResponseEntity.ok(Map.of("message", "动作创建成功", "exerciseId", exercise.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // AI辅助创建自定义动作
    @PostMapping("/ai/create")
    public ResponseEntity<?> createExerciseWithAI(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            String description = (String) request.get("description");
            
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "用户不存在"));
            }
            
            Exercise exercise = exerciseService.createExerciseWithAI(user, description);
            return ResponseEntity.ok(Map.of("message", "动作创建成功", "exerciseId", exercise.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // AI动作推荐
    @PostMapping("/ai/recommend")
    public ResponseEntity<?> recommendExercises(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            String userQuery = (String) request.get("query");
            
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "用户不存在"));
            }
            
            List<Exercise> exercises = exerciseService.recommendExercises(userQuery, user);
            return ResponseEntity.ok(exercises);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 更新动作
    @PutMapping("/{id}")
    public ResponseEntity<?> updateExercise(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String category = (String) request.get("category");
            String equipment = (String) request.get("equipment");
            String difficulty = (String) request.get("difficulty");
            String description = (String) request.get("description");
            String instructions = (String) request.get("instructions");
            String tips = (String) request.get("tips");
            String precautions = (String) request.get("precautions");
            
            Exercise exercise = exerciseService.updateExercise(id, name, category, equipment, 
                                                           difficulty, description, instructions, 
                                                           tips, precautions);
            return ResponseEntity.ok(Map.of("message", "动作更新成功", "exerciseId", exercise.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 删除动作
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExercise(@PathVariable Long id) {
        try {
            exerciseService.deleteExercise(id);
            return ResponseEntity.ok(Map.of("message", "动作删除成功"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
}
