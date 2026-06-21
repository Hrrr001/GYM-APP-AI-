package com.gym.controller;

import com.gym.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {
    @Autowired
    private AIService aiService;
    
    // 回答健身相关问题
    @PostMapping("/question")
    public ResponseEntity<?> answerFitnessQuestion(@RequestBody Map<String, Object> request) {
        try {
            String question = (String) request.get("question");
            if (question == null || question.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "问题不能为空"));
            }
            
            // 检查是否与健身相关
            if (!isFitnessRelated(question)) {
                return ResponseEntity.ok(Map.of("answer", "抱歉，我只能回答与健身相关的问题。请问您有什么健身方面的问题需要咨询？"));
            }
            
            String answer = aiService.answerFitnessQuestion(question);
            return ResponseEntity.ok(Map.of("answer", answer));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 检查是否与健身相关
    private boolean isFitnessRelated(String text) {
        // 健身相关关键词
        String[] fitnessKeywords = {
            "健身", "锻炼", "训练", "运动", "减脂", "增肌", "塑形", "耐力", "力量",
            "饮食", "营养", "食谱", "卡路里", "蛋白质", "碳水", "脂肪", "肌肉",
            "有氧", "无氧", "器械", "哑铃", "杠铃", "俯卧撑", "深蹲", "硬拉",
            "计划", "目标", "效果", "恢复", "拉伸", "热身", "心率", "代谢"
        };
        
        // 检查是否包含健身相关关键词
        for (String keyword : fitnessKeywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    // 生成动作详情
    @PostMapping("/exercise/details")
    public ResponseEntity<?> generateExerciseDetails(@RequestBody Map<String, Object> request) {
        try {
            String description = (String) request.get("description");
            if (description == null || description.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "描述不能为空"));
            }
            
            String details = aiService.generateExerciseDetails(description);
            return ResponseEntity.ok(Map.of("details", details));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 推荐动作
    @PostMapping("/exercise/recommend")
    public ResponseEntity<?> recommendExercises(@RequestBody Map<String, Object> request) {
        try {
            String userQuery = (String) request.get("userQuery");
            String userProfile = (String) request.get("userProfile");
            
            if (userQuery == null || userQuery.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "用户查询不能为空"));
            }
            
            String recommendations = aiService.recommendExercises(userQuery, userProfile);
            return ResponseEntity.ok(Map.of("recommendations", recommendations));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 生成训练计划
    @PostMapping("/plan/generate")
    public ResponseEntity<?> generateTrainingPlan(@RequestBody Map<String, Object> request) {
        try {
            String userProfile = (String) request.get("userProfile");
            String goal = (String) request.get("goal");
            int duration = Integer.parseInt(request.get("duration").toString());
            
            if (userProfile == null || userProfile.isEmpty() || goal == null || goal.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "用户信息和目标不能为空"));
            }
            
            String plan = aiService.generateTrainingPlan(userProfile, goal, duration);
            return ResponseEntity.ok(Map.of("plan", plan));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 分析训练数据
    @PostMapping("/workout/analyze")
    public ResponseEntity<?> analyzeWorkoutData(@RequestBody Map<String, Object> request) {
        try {
            String workoutData = (String) request.get("workoutData");
            if (workoutData == null || workoutData.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "训练数据不能为空"));
            }
            
            String analysis = aiService.analyzeWorkoutData(workoutData);
            return ResponseEntity.ok(Map.of("analysis", analysis));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    // 生成饮食建议
    @PostMapping("/nutrition/suggestion")
    public ResponseEntity<?> generateDietSuggestion(@RequestBody Map<String, Object> request) {
        try {
            String userProfile = (String) request.get("userProfile");
            String currentDiet = (String) request.get("currentDiet");
            
            if (userProfile == null || userProfile.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "用户信息不能为空"));
            }
            
            String suggestion = aiService.generateDietSuggestion(userProfile, currentDiet);
            return ResponseEntity.ok(Map.of("suggestion", suggestion));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
}