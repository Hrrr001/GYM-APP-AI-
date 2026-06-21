package com.gym.controller;

import com.gym.entity.User;
import com.gym.entity.UserProfile;
import com.gym.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            String email = request.get("email");
            
            User user = userService.register(username, password, email);
            return ResponseEntity.ok(Map.of("message", "注册成功", "userId", user.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            
            User user = userService.login(username, password);
            return ResponseEntity.ok(Map.of("message", "登录成功", "userId", user.getId(), "username", user.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        }
    }
    
    @PostMapping("/profile")
    public ResponseEntity<?> createProfile(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            String name = (String) request.get("name");
            String gender = (String) request.get("gender");
            int age = Integer.parseInt(request.get("age").toString());
            double height = Double.parseDouble(request.get("height").toString());
            double weight = Double.parseDouble(request.get("weight").toString());
            String fitnessLevel = (String) request.get("fitnessLevel");
            String fitnessGoal = (String) request.get("fitnessGoal");
            String injuries = (String) request.get("injuries");
            String equipmentCondition = (String) request.get("equipmentCondition");
            
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "用户不存在"));
            }
            
            UserProfile profile = userService.createProfile(user, name, gender, age, height, weight, 
                                                          fitnessLevel, fitnessGoal, injuries, equipmentCondition);
            return ResponseEntity.ok(Map.of("message", "档案创建成功", "profileId", profile.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            String name = (String) request.get("name");
            String gender = (String) request.get("gender");
            int age = Integer.parseInt(request.get("age").toString());
            double height = Double.parseDouble(request.get("height").toString());
            double weight = Double.parseDouble(request.get("weight").toString());
            String fitnessLevel = (String) request.get("fitnessLevel");
            String fitnessGoal = (String) request.get("fitnessGoal");
            String injuries = (String) request.get("injuries");
            String equipmentCondition = (String) request.get("equipmentCondition");
            
            UserProfile profile = userService.updateProfile(userId, name, gender, age, height, weight, 
                                                          fitnessLevel, fitnessGoal, injuries, equipmentCondition);
            return ResponseEntity.ok(Map.of("message", "档案更新成功", "profileId", profile.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
    
    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable Long userId) {
        try {
            UserProfile profile = userService.getProfile(userId);
            if (profile == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "用户档案不存在"));
            }
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }
    }
}
