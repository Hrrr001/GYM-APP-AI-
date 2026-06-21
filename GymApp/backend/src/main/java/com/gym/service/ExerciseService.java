package com.gym.service;

import com.gym.entity.Exercise;
import com.gym.entity.User;
import com.gym.repository.ExerciseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExerciseService {
    @Autowired
    private ExerciseRepository exerciseRepository;
    
    @Autowired
    private AIService aiService;
    
    // 获取所有标准动作
    public List<Exercise> getStandardExercises() {
        return exerciseRepository.findByIsCustomFalse();
    }
    
    // 根据类别获取动作
    public List<Exercise> getExercisesByCategory(String category) {
        return exerciseRepository.findByCategory(category);
    }
    
    // 根据器械获取动作
    public List<Exercise> getExercisesByEquipment(String equipment) {
        return exerciseRepository.findByEquipment(equipment);
    }
    
    // 根据难度获取动作
    public List<Exercise> getExercisesByDifficulty(String difficulty) {
        return exerciseRepository.findByDifficulty(difficulty);
    }
    
    // 获取用户自定义动作
    public List<Exercise> getCustomExercises(Long userId) {
        return exerciseRepository.findByCreatorId(userId);
    }
    
    // 根据ID获取动作
    public Exercise getExerciseById(Long id) {
        Optional<Exercise> optionalExercise = exerciseRepository.findById(id);
        return optionalExercise.orElse(null);
    }
    
    // 创建自定义动作
    public Exercise createCustomExercise(User user, String name, String category, String equipment, 
                                       String difficulty, String description, String instructions, 
                                       String tips, String precautions) {
        Exercise exercise = new Exercise();
        exercise.setName(name);
        exercise.setCategory(category);
        exercise.setEquipment(equipment);
        exercise.setDifficulty(difficulty);
        exercise.setDescription(description);
        exercise.setInstructions(instructions);
        exercise.setTips(tips);
        exercise.setPrecautions(precautions);
        exercise.setCustom(true);
        exercise.setCreator(user);
        
        return exerciseRepository.save(exercise);
    }
    
    // AI辅助创建自定义动作
    public Exercise createExerciseWithAI(User user, String description) {
        // 调用AI服务生成动作详情
        String aiResponse = aiService.generateExerciseDetails(description);
        
        // 解析AI响应，提取动作信息
        // 这里需要根据实际的AI响应格式进行解析
        // 暂时使用模拟数据
        Exercise exercise = new Exercise();
        exercise.setName(description);
        exercise.setCategory("其他");
        exercise.setEquipment("无器械");
        exercise.setDifficulty("beginner");
        exercise.setDescription(description);
        exercise.setInstructions("按照AI建议执行");
        exercise.setTips("保持正确姿势");
        exercise.setPrecautions("如有不适请停止");
        exercise.setCustom(true);
        exercise.setCreator(user);
        
        return exerciseRepository.save(exercise);
    }
    
    // AI动作推荐
    public List<Exercise> recommendExercises(String userQuery, User user) {
        // 调用AI服务获取推荐动作
        // 这里需要根据实际的AI响应格式进行处理
        // 暂时返回所有标准动作
        return exerciseRepository.findByIsCustomFalse();
    }
    
    // 更新动作
    public Exercise updateExercise(Long id, String name, String category, String equipment, 
                                 String difficulty, String description, String instructions, 
                                 String tips, String precautions) {
        Optional<Exercise> optionalExercise = exerciseRepository.findById(id);
        if (optionalExercise.isEmpty()) {
            throw new RuntimeException("动作不存在");
        }
        
        Exercise exercise = optionalExercise.get();
        exercise.setName(name);
        exercise.setCategory(category);
        exercise.setEquipment(equipment);
        exercise.setDifficulty(difficulty);
        exercise.setDescription(description);
        exercise.setInstructions(instructions);
        exercise.setTips(tips);
        exercise.setPrecautions(precautions);
        
        return exerciseRepository.save(exercise);
    }
    
    // 删除动作
    public void deleteExercise(Long id) {
        exerciseRepository.deleteById(id);
    }
}
