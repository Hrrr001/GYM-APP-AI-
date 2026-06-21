package com.gym.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIService {

    @Value("${deepseek.api.key}")
    private String apiKey;

    @Value("${python.ai.base-url:http://localhost:8000}")
    private String pythonAiBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // ========================
    // 训练计划
    // ========================

    public Map<String, Object> generateTrainingPlan(Map<String, Object> request) {
        return postForMap("/api/ai/plan/generate", request);
    }

    // ========================
    // 多轮健身问答
    // ========================

    public Map<String, Object> askFitnessQuestion(Map<String, Object> request) {
        return postForMap("/api/ai/qa/ask", request);
    }

    public Map<String, Object> createQASession(Integer userId) {
        String url = pythonAiBaseUrl + "/api/ai/qa/session/new";
        if (userId != null) {
            url += "?user_id=" + userId;
        }
        try {
            return restTemplate.postForObject(url, null, Map.class);
        } catch (Exception e) {
            return Map.of("session_id", "", "message", "创建会话失败: " + e.getMessage());
        }
    }

    public Map<String, Object> getQASession(String sessionId) {
        return getForMap("/api/ai/qa/session/" + sessionId);
    }

    public List<Map<String, Object>> listQASessions(Integer userId) {
        String url = pythonAiBaseUrl + "/api/ai/qa/sessions";
        if (userId != null) {
            url += "?user_id=" + userId;
        }
        try {
            return restTemplate.getForObject(url, List.class);
        } catch (Exception e) {
            return List.of();
        }
    }

    // ========================
    // 训练反馈闭环
    // ========================

    public Map<String, Object> submitFeedback(Map<String, Object> request) {
        return postForMap("/api/ai/feedback/submit", request);
    }

    public Map<String, Object> adjustPlan(Map<String, Object> request) {
        return postForMap("/api/ai/feedback/adjust-plan", request);
    }

    // ========================
    // 兼容旧接口（转发到 Python 服务）
    // ========================

    public String answerFitnessQuestion(String question) {
        Map<String, Object> body = Map.of(
            "session_id", "legacy-" + System.currentTimeMillis(),
            "question", question
        );
        Map<String, Object> result = postForMap("/api/ai/qa/ask", body);
        Object answer = result.get("answer");
        return answer != null ? answer.toString() : "AI服务暂时不可用，请稍后重试。";
    }

    public String generateExerciseDetails(String description) {
        Map<String, Object> body = Map.of(
            "session_id", "legacy-" + System.currentTimeMillis(),
            "question", "请为以下健身动作生成详细说明：训练动作、目标肌群、动作要领、安全注意事项——" + description
        );
        Map<String, Object> result = postForMap("/api/ai/qa/ask", body);
        Object answer = result.get("answer");
        return answer != null ? answer.toString() : "AI服务暂时不可用，请稍后重试。";
    }

    public String recommendExercises(String userQuery, String userProfile) {
        Map<String, Object> body = Map.of(
            "session_id", "legacy-" + System.currentTimeMillis(),
            "question", "用户需求：" + userQuery + "，用户资料：" + userProfile + "，请推荐适合的健身动作。"
        );
        Map<String, Object> result = postForMap("/api/ai/qa/ask", body);
        Object answer = result.get("answer");
        return answer != null ? answer.toString() : "AI服务暂时不可用，请稍后重试。";
    }

    public String generateTrainingPlan(String userProfile, String goal, int duration) {
        Map<String, Object> body = Map.of(
            "goal", goal,
            "duration", duration,
            "additional_requirements", "用户资料：" + userProfile
        );
        Map<String, Object> result = postForMap("/api/ai/plan/generate", body);
        if (result.containsKey("plan")) {
            return result.get("plan").toString();
        }
        Object answer = result.get("answer");
        return answer != null ? answer.toString() : "AI服务暂时不可用，请稍后重试。";
    }

    public String analyzeWorkoutData(String workoutData) {
        Map<String, Object> body = Map.of(
            "session_id", "legacy-" + System.currentTimeMillis(),
            "question", "请分析以下训练数据并提供改进建议：" + workoutData
        );
        Map<String, Object> result = postForMap("/api/ai/qa/ask", body);
        Object answer = result.get("answer");
        return answer != null ? answer.toString() : "AI服务暂时不可用，请稍后重试。";
    }

    public String generateDietSuggestion(String userProfile, String currentDiet) {
        Map<String, Object> body = Map.of(
            "session_id", "legacy-" + System.currentTimeMillis(),
            "question", "用户资料：" + userProfile + "，当前饮食：" + currentDiet + "，请生成饮食建议。"
        );
        Map<String, Object> result = postForMap("/api/ai/qa/ask", body);
        Object answer = result.get("answer");
        return answer != null ? answer.toString() : "AI服务暂时不可用，请稍后重试。";
    }

    // ========================
    // 内部 HTTP 工具方法
    // ========================

    private Map<String, Object> postForMap(String path, Map<String, Object> body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(
                pythonAiBaseUrl + path, HttpMethod.POST, entity, Map.class
            );
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "AI 服务连接失败: " + e.getMessage());
        }
    }

    private Map<String, Object> getForMap(String path) {
        try {
            return restTemplate.getForObject(pythonAiBaseUrl + path, Map.class);
        } catch (Exception e) {
            return Map.of("error", "AI 服务连接失败: " + e.getMessage());
        }
    }
}
