package com.gym.service;

import com.gym.entity.User;
import com.gym.entity.UserProfile;
import com.gym.repository.UserRepository;
import com.gym.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserProfileRepository userProfileRepository;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public User register(String username, String password, String email) {
        // 检查用户名是否已存在
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("邮箱已被注册");
        }
        
        // 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        
        return userRepository.save(user);
    }
    
    public User login(String username, String password) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        User user = optionalUser.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        return user;
    }
    
    public UserProfile createProfile(User user, String name, String gender, int age, double height, double weight, 
                                   String fitnessLevel, String fitnessGoal, String injuries, String equipmentCondition) {
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setName(name);
        profile.setGender(gender);
        profile.setAge(age);
        profile.setHeight(height);
        profile.setWeight(weight);
        profile.setFitnessLevel(fitnessLevel);
        profile.setFitnessGoal(fitnessGoal);
        profile.setInjuries(injuries);
        profile.setEquipmentCondition(equipmentCondition);
        
        return userProfileRepository.save(profile);
    }
    
    public UserProfile updateProfile(Long userId, String name, String gender, int age, double height, double weight, 
                                   String fitnessLevel, String fitnessGoal, String injuries, String equipmentCondition) {
        Optional<UserProfile> optionalProfile = userProfileRepository.findByUserId(userId);
        if (optionalProfile.isEmpty()) {
            throw new RuntimeException("用户档案不存在");
        }
        
        UserProfile profile = optionalProfile.get();
        profile.setName(name);
        profile.setGender(gender);
        profile.setAge(age);
        profile.setHeight(height);
        profile.setWeight(weight);
        profile.setFitnessLevel(fitnessLevel);
        profile.setFitnessGoal(fitnessGoal);
        profile.setInjuries(injuries);
        profile.setEquipmentCondition(equipmentCondition);
        
        return userProfileRepository.save(profile);
    }
    
    public UserProfile getProfile(Long userId) {
        Optional<UserProfile> optionalProfile = userProfileRepository.findByUserId(userId);
        return optionalProfile.orElse(null);
    }
    
    public User getUserById(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        return optionalUser.orElse(null);
    }
}
