# 数据库设计文档

## 1. 数据库表结构

### 1.1 用户表 (users)
| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | 用户ID |
| username | VARCHAR(50) | UNIQUE, NOT NULL | 用户名 |
| password | VARCHAR(100) | NOT NULL | 密码（加密存储） |
| email | VARCHAR(100) | UNIQUE, NOT NULL | 邮箱 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 1.2 用户健康档案表 (user_profiles)
| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | 档案ID |
| user_id | INT | FOREIGN KEY (users.id) | 用户ID |
| name | VARCHAR(50) | NOT NULL | 姓名 |
| gender | ENUM('male', 'female') | NOT NULL | 性别 |
| age | INT | NOT NULL | 年龄 |
| height | DECIMAL(5,2) | NOT NULL | 身高(cm) |
| weight | DECIMAL(5,2) | NOT NULL | 体重(kg) |
| fitness_level | ENUM('beginner', 'intermediate', 'advanced') | NOT NULL | 健身水平 |
| fitness_goal | VARCHAR(100) | NOT NULL | 健身目标 |
| injuries | TEXT | | 伤病情况 |
| equipment_condition | TEXT | | 器械条件 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 1.3 动作表 (exercises)
| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | 动作ID |
| name | VARCHAR(100) | NOT NULL | 动作名称 |
| category | VARCHAR(50) | NOT NULL | 动作类别（如：胸部、背部、腿部等） |
| equipment | VARCHAR(50) | NOT NULL | 所需器械 |
| difficulty | ENUM('beginner', 'intermediate', 'advanced') | NOT NULL | 难度等级 |
| description | TEXT | NOT NULL | 动作描述 |
| instructions | TEXT | NOT NULL | 动作步骤 |
| tips | TEXT | | 发力要点 |
| precautions | TEXT | | 注意事项 |
| is_custom | BOOLEAN | DEFAULT FALSE | 是否自定义动作 |
| creator_id | INT | FOREIGN KEY (users.id) | 自定义动作创建者ID |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 1.4 训练计划表 (training_plans)
| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | 计划ID |
| user_id | INT | FOREIGN KEY (users.id) | 用户ID |
| name | VARCHAR(100) | NOT NULL | 计划名称 |
| goal | VARCHAR(100) | NOT NULL | 计划目标 |
| duration | INT | NOT NULL | 计划周期(周) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 1.5 计划详情表 (plan_details)
| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | 详情ID |
| plan_id | INT | FOREIGN KEY (training_plans.id) | 计划ID |
| week | INT | NOT NULL | 周数 |
| day | INT | NOT NULL | 天数 |
| exercise_id | INT | FOREIGN KEY (exercises.id) | 动作ID |
| sets | INT | NOT NULL | 组数 |
| reps | INT | NOT NULL | 次数 |
| weight | DECIMAL(5,2) | | 重量 |
| rest_time | INT | | 组间休息时间(秒) |

### 1.6 训练打卡表 (workout_records)
| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | 记录ID |
| user_id | INT | FOREIGN KEY (users.id) | 用户ID |
| plan_id | INT | FOREIGN KEY (training_plans.id) | 计划ID |
| date | DATE | NOT NULL | 打卡日期 |
| duration | INT | NOT NULL | 训练时长(分钟) |
| feeling | TEXT | | 训练感受 |
| notes | TEXT | | 备注 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 1.7 打卡详情表 (workout_details)
| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | 详情ID |
| record_id | INT | FOREIGN KEY (workout_records.id) | 打卡记录ID |
| exercise_id | INT | FOREIGN KEY (exercises.id) | 动作ID |
| sets | INT | NOT NULL | 实际完成组数 |
| reps | INT | NOT NULL | 实际完成次数 |
| weight | DECIMAL(5,2) | | 实际使用重量 |
| completed | BOOLEAN | DEFAULT TRUE | 是否完成 |

### 1.8 饮食记录表 (food_records)
| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | 记录ID |
| user_id | INT | FOREIGN KEY (users.id) | 用户ID |
| date | DATE | NOT NULL | 记录日期 |
| meal_type | ENUM('breakfast', 'lunch', 'dinner', 'snack') | NOT NULL | 餐次类型 |
| food_name | VARCHAR(100) | NOT NULL | 食物名称 |
| calories | DECIMAL(6,2) | NOT NULL | 热量(卡路里) |
| protein | DECIMAL(5,2) | | 蛋白质(克) |
| carbs | DECIMAL(5,2) | | 碳水化合物(克) |
| fat | DECIMAL(5,2) | | 脂肪(克) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

### 1.9 身体数据表 (body_measurements)
| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|------|------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | 记录ID |
| user_id | INT | FOREIGN KEY (users.id) | 用户ID |
| date | DATE | NOT NULL | 记录日期 |
| weight | DECIMAL(5,2) | NOT NULL | 体重(kg) |
| body_fat | DECIMAL(4,2) | | 体脂率(%) |
| muscle_mass | DECIMAL(5,2) | | 肌肉量(kg) |
| waist | DECIMAL(5,2) | | 腰围(cm) |
| hip | DECIMAL(5,2) | | 臀围(cm) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 创建时间 |

## 2. 数据库索引设计

为了提高查询性能，建议在以下字段上创建索引：

1. `users`表：`username`、`email`
2. `user_profiles`表：`user_id`
3. `exercises`表：`category`、`equipment`、`difficulty`
4. `training_plans`表：`user_id`
5. `plan_details`表：`plan_id`、`exercise_id`
6. `workout_records`表：`user_id`、`plan_id`、`date`
7. `workout_details`表：`record_id`、`exercise_id`
8. `food_records`表：`user_id`、`date`
9. `body_measurements`表：`user_id`、`date`

## 3. 数据库关系图

```
users ──┬── user_profiles
        ├── exercises (creator_id)
        ├── training_plans
        ├── workout_records
        ├── food_records
        └── body_measurements

exercises ──┬── plan_details
            └── workout_details

training_plans ──┬── plan_details
                 └── workout_records

workout_records ── workout_details
```
