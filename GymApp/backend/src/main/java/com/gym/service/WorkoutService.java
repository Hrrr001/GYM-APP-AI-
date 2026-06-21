package com.gym.service;

import com.gym.entity.WorkoutRecord;
import com.gym.entity.WorkoutDetail;
import com.gym.entity.User;
import com.gym.entity.TrainingPlan;
import com.gym.entity.Exercise;
import com.gym.repository.WorkoutRecordRepository;
import com.gym.repository.WorkoutDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Service
public class WorkoutService {
    @Autowired
    private WorkoutRecordRepository workoutRecordRepository;
    
    @Autowired
    private WorkoutDetailRepository workoutDetailRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TrainingPlanService trainingPlanService;
    
    @Autowired
    private ExerciseService exerciseService;
    
    @Autowired
    private AIService aiService;
    
    // 获取用户的所有训练记录
    public List<WorkoutRecord> getUserRecords(Long userId) {
        return workoutRecordRepository.findByUserId(userId);
    }
    
    // 根据日期范围获取用户的训练记录
    public List<WorkoutRecord> getUserRecordsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return workoutRecordRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    }
    
    // 根据计划获取训练记录
    public List<WorkoutRecord> getRecordsByPlan(Long planId) {
        return workoutRecordRepository.findByPlanId(planId);
    }
    
    // 根据ID获取训练记录
    public WorkoutRecord getRecordById(Long recordId) {
        Optional<WorkoutRecord> optionalRecord = workoutRecordRepository.findById(recordId);
        return optionalRecord.orElse(null);
    }
    
    // 获取训练记录详情
    public List<WorkoutDetail> getRecordDetails(Long recordId) {
        return workoutDetailRepository.findByRecordId(recordId);
    }
    
    // 创建训练记录
    public WorkoutRecord createRecord(User user, Long planId, LocalDate date, int duration, String feeling, String notes) {
        WorkoutRecord record = new WorkoutRecord();
        record.setUser(user);
        
        if (planId != null) {
            TrainingPlan plan = trainingPlanService.getPlanById(planId);
            if (plan != null) {
                record.setPlan(plan);
            }
        }
        
        record.setDate(date);
        record.setDuration(duration);
        record.setFeeling(feeling);
        record.setNotes(notes);
        
        return workoutRecordRepository.save(record);
    }
    
    // 添加训练记录详情
    public WorkoutDetail addRecordDetail(WorkoutRecord record, Long exerciseId, int sets, int reps, Double weight, boolean completed) {
        Exercise exercise = exerciseService.getExerciseById(exerciseId);
        if (exercise == null) {
            throw new RuntimeException("动作不存在");
        }
        
        WorkoutDetail detail = new WorkoutDetail();
        detail.setRecord(record);
        detail.setExercise(exercise);
        detail.setSets(sets);
        detail.setReps(reps);
        detail.setWeight(weight);
        detail.setCompleted(completed);
        
        return workoutDetailRepository.save(detail);
    }
    
    // 获取AI训练反馈
    public String getAIFeedback(Long recordId) {
        WorkoutRecord record = getRecordById(recordId);
        if (record == null) {
            throw new RuntimeException("训练记录不存在");
        }
        
        List<WorkoutDetail> details = getRecordDetails(recordId);
        
        // 构建训练数据字符串
        StringBuilder workoutData = new StringBuilder();
        workoutData.append("训练日期: " + record.getDate() + "\n");
        workoutData.append("训练时长: " + record.getDuration() + "分钟\n");
        workoutData.append("训练感受: " + record.getFeeling() + "\n");
        workoutData.append("训练内容: \n");
        
        for (WorkoutDetail detail : details) {
            workoutData.append("- " + detail.getExercise().getName() + ": " + detail.getSets() + "组 × " + detail.getReps() + "次");
            if (detail.getWeight() != null) {
                workoutData.append(" (" + detail.getWeight() + "kg)");
            }
            workoutData.append("\n");
        }
        
        // 调用AI服务获取反馈
        return aiService.analyzeWorkoutData(workoutData.toString());
    }
    
    // 更新训练记录
    public WorkoutRecord updateRecord(Long recordId, Integer duration, String feeling, String notes) {
        Optional<WorkoutRecord> optionalRecord = workoutRecordRepository.findById(recordId);
        if (optionalRecord.isEmpty()) {
            throw new RuntimeException("训练记录不存在");
        }
        
        WorkoutRecord record = optionalRecord.get();
        if (duration != null) record.setDuration(duration);
        if (feeling != null) record.setFeeling(feeling);
        if (notes != null) record.setNotes(notes);
        
        return workoutRecordRepository.save(record);
    }
    
    // 更新训练记录详情
    public WorkoutDetail updateRecordDetail(Long detailId, Integer sets, Integer reps, Double weight, Boolean completed) {
        Optional<WorkoutDetail> optionalDetail = workoutDetailRepository.findById(detailId);
        if (optionalDetail.isEmpty()) {
            throw new RuntimeException("训练记录详情不存在");
        }
        
        WorkoutDetail detail = optionalDetail.get();
        if (sets != null) detail.setSets(sets);
        if (reps != null) detail.setReps(reps);
        if (weight != null) detail.setWeight(weight);
        if (completed != null) detail.setCompleted(completed);
        
        return workoutDetailRepository.save(detail);
    }
    
    // 删除训练记录
    public void deleteRecord(Long recordId) {
        // 先删除记录详情
        List<WorkoutDetail> details = workoutDetailRepository.findByRecordId(recordId);
        workoutDetailRepository.deleteAll(details);
        // 再删除记录
        workoutRecordRepository.deleteById(recordId);
    }
    
    // 删除训练记录详情
    public void deleteRecordDetail(Long detailId) {
        workoutDetailRepository.deleteById(detailId);
    }
}
