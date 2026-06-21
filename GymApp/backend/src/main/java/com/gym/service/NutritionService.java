package com.gym.service;

import com.gym.entity.FoodRecord;
import com.gym.entity.BodyMeasurement;
import com.gym.entity.User;
import com.gym.repository.FoodRecordRepository;
import com.gym.repository.BodyMeasurementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Service
public class NutritionService {
    @Autowired
    private FoodRecordRepository foodRecordRepository;
    
    @Autowired
    private BodyMeasurementRepository bodyMeasurementRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AIService aiService;
    
    // 获取用户的所有饮食记录
    public List<FoodRecord> getUserFoodRecords(Long userId) {
        return foodRecordRepository.findByUserId(userId);
    }
    
    // 获取用户某天的饮食记录
    public List<FoodRecord> getUserFoodRecordsByDate(Long userId, LocalDate date) {
        return foodRecordRepository.findByUserIdAndDate(userId, date);
    }
    
    // 获取用户某时间段的饮食记录
    public List<FoodRecord> getUserFoodRecordsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return foodRecordRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    }
    
    // 获取用户某餐的饮食记录
    public List<FoodRecord> getUserFoodRecordsByMealType(Long userId, String mealType) {
        return foodRecordRepository.findByUserIdAndMealType(userId, mealType);
    }
    
    // 创建饮食记录
    public FoodRecord createFoodRecord(User user, LocalDate date, String mealType, String foodName, 
                                     double calories, Double protein, Double carbs, Double fat) {
        FoodRecord record = new FoodRecord();
        record.setUser(user);
        record.setDate(date);
        record.setMealType(mealType);
        record.setFoodName(foodName);
        record.setCalories(calories);
        record.setProtein(protein);
        record.setCarbs(carbs);
        record.setFat(fat);
        
        return foodRecordRepository.save(record);
    }
    
    // AI辅助创建饮食记录
    public FoodRecord createFoodRecordWithAI(User user, LocalDate date, String mealType, String description) {
        // 调用AI服务解析饮食描述
        // 暂时使用模拟数据
        FoodRecord record = new FoodRecord();
        record.setUser(user);
        record.setDate(date);
        record.setMealType(mealType);
        record.setFoodName(description);
        record.setCalories(500.0);
        record.setProtein(20.0);
        record.setCarbs(60.0);
        record.setFat(20.0);
        
        return foodRecordRepository.save(record);
    }
    
    // 获取AI饮食建议
    public String getAIDietSuggestion(Long userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 获取用户最近的饮食记录
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(7);
        List<FoodRecord> recentRecords = foodRecordRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
        
        // 构建饮食数据字符串
        StringBuilder dietData = new StringBuilder();
        dietData.append("用户最近一周的饮食记录：\n");
        for (FoodRecord record : recentRecords) {
            dietData.append(record.getDate() + " " + record.getMealType() + ": " + record.getFoodName() + " (" + record.getCalories() + "卡路里)\n");
        }
        
        // 调用AI服务获取饮食建议
        return aiService.generateDietSuggestion(user.toString(), dietData.toString());
    }
    
    // 检查是否与饮食相关
    public boolean isDietRelated(String text) {
        // 饮食相关关键词
        String[] dietKeywords = {
            "饮食", "营养", "食谱", "卡路里", "蛋白质", "碳水", "脂肪", "食物",
            "早餐", "午餐", "晚餐", "零食", "饮食计划", "营养建议", "膳食", "餐饮",
            "吃", "喝", "餐", "饭", "菜", "肉", "鱼", "蛋", "奶", "水果", "蔬菜",
            "面", "饭", "粥", "汤", "汉堡", "披萨", "沙拉", "三明治", "牛排", "炸鸡"
        };
        
        // 检查是否包含饮食相关关键词
        for (String keyword : dietKeywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        
        // 检查是否包含常见的饮食描述词汇
        String[] commonDietPhrases = {
            "今天吃", "我吃了", "吃了", "喝了", "午餐", "晚餐", "早餐", "零食",
            "吃的", "喝的", "饮食", "食物", "餐", "饭", "菜"
        };
        
        for (String phrase : commonDietPhrases) {
            if (text.contains(phrase)) {
                return true;
            }
        }
        
        return false;
    }
    
    // 删除饮食记录
    public void deleteFoodRecord(Long recordId) {
        foodRecordRepository.deleteById(recordId);
    }
    
    // 获取用户的所有身体数据记录
    public List<BodyMeasurement> getUserBodyMeasurements(Long userId) {
        return bodyMeasurementRepository.findByUserId(userId);
    }
    
    // 获取用户某时间段的身体数据记录
    public List<BodyMeasurement> getUserBodyMeasurementsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return bodyMeasurementRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    }
    
    // 创建身体数据记录
    public BodyMeasurement createBodyMeasurement(User user, LocalDate date, double weight, 
                                              Double bodyFat, Double muscleMass, Double waist, Double hip) {
        BodyMeasurement measurement = new BodyMeasurement();
        measurement.setUser(user);
        measurement.setDate(date);
        measurement.setWeight(weight);
        measurement.setBodyFat(bodyFat);
        measurement.setMuscleMass(muscleMass);
        measurement.setWaist(waist);
        measurement.setHip(hip);
        
        return bodyMeasurementRepository.save(measurement);
    }
    
    // 删除身体数据记录
    public void deleteBodyMeasurement(Long measurementId) {
        bodyMeasurementRepository.deleteById(measurementId);
    }
}
