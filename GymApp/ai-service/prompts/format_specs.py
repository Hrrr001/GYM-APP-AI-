# === 训练计划输出 JSON Schema ===
TRAINING_PLAN_FORMAT = {
    "plan_name": "计划名称（如：4周增肌训练计划）",
    "plan_overview": "计划总体说明（200字以内）",
    "safety_notes": ["安全注意事项1", "安全注意事项2"],
    "warmup_routine": {
        "duration_minutes": 10,
        "exercises": [
            {"name": "动态拉伸-高抬腿", "duration": "60秒", "sets": 1},
            {"name": "关节活动-肩绕环", "duration": "30秒", "sets": 2},
        ],
    },
    "cooldown_routine": {
        "duration_minutes": 10,
        "exercises": [
            {"name": "静态拉伸-股四头肌", "duration": "30秒/侧", "sets": 2},
        ],
    },
    "weekly_plans": [
        {
            "week": 1,
            "theme": "本周训练主题（如：适应性训练，建立运动模式）",
            "days": [
                {
                    "day": 1,
                    "focus": "训练重点（如：上肢推+核心）",
                    "exercises": [
                        {
                            "name": "动作名称",
                            "sets": 3,
                            "reps": "12-15",
                            "weight": "自体重或建议重量",
                            "rest_seconds": 60,
                            "notes": "动作要点或替换动作",
                            "safety_tip": "安全提示（如有风险）",
                        }
                    ],
                }
            ],
        }
    ],
}

# === 健身咨询回答格式 ===
FITNESS_ANSWER_FORMAT = {
    "answer": "详细回答内容",
    "references": ["引用的知识库条目ID1", "引用的知识库条目ID2"],
    "graph_reasoning_paths": ["知识图谱推理链1", "知识图谱推理链2"],
    "confidence": "高/中/低 — 表示回答的确定程度",
    "safety_warnings": ["安全警告1", "安全警告2"],
    "related_questions": ["相关问题1", "相关问题2"],
    "disclaimer": "如有专业知识不足，注明建议咨询专业人士",
}

# === 计划调整输出格式 ===
PLAN_ADJUST_FORMAT = {
    "adjustment_summary": "调整总结",
    "adjustments_made": [
        {
            "type": "动作替换/强度调整/频率调整",
            "original": "原来的内容",
            "adjusted": "调整后的内容",
            "reason": "调整原因（关联用户反馈）",
        }
    ],
    "adjusted_plan": "{与TRAINING_PLAN_FORMAT结构相同}",
    "follow_up_questions": ["跟进问题，询问用户下一步感受"],
}
