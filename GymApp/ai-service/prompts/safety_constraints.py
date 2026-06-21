from typing import Dict

# === 高风险动作列表（运动医学共识） ===
HIGH_RISK_EXERCISES = {
    "颈后下拉": "肩关节前囊过度牵拉，易导致肩袖损伤",
    "颈后推举": "肩关节极限外旋位，易导致肩峰撞击综合征",
    "直腿仰卧起坐": "过度屈髋导致腰椎前凸，增加椎间盘压力",
    "罗马尼亚硬拉（新手）": "新手动作模式不正确易导致腰椎损伤",
    "史密斯机深蹲": "限制自然运动轨迹，增加膝关节剪切力",
    "器械腿伸展（全范围）": "膝关节终末伸展位对前交叉韧带压力过大",
    "直立划船（窄握）": "肩关节内旋+外展位，易导致肩峰撞击",
}


def check_exercise_safety(exercise_name: str) -> Dict:
    """检查动作安全性，返回风险评估"""
    for risky_exercise, reason in HIGH_RISK_EXERCISES.items():
        if risky_exercise in exercise_name:
            return {
                "is_safe": False,
                "risk": reason,
                "recommendation": "建议替换为更安全的替代动作，或由专业教练一对一指导",
            }
    return {"is_safe": True}


def check_volume_safety(current_volume: int, new_volume: int) -> Dict:
    """检查训练量增幅是否在安全范围内（≤10%）"""
    if current_volume == 0:
        return {"is_safe": True}
    increase_pct = (new_volume - current_volume) / current_volume * 100
    if increase_pct > 10:
        return {
            "is_safe": False,
            "current_volume": current_volume,
            "new_volume": new_volume,
            "increase_pct": round(increase_pct, 1),
            "reason": f"训练量增幅{increase_pct:.1f}%超过安全上限10%",
        }
    return {"is_safe": True}


# === 初学者安全约束 ===
BEGINNER_SAFETY_RULES = [
    "前三周以动作学习为主，每组15-20次轻重量，重点建立正确运动模式",
    "避免自由重量复合动作（如杠铃深蹲、硬拉），优先使用固定器械",
    "每周训练不超过3次，确保充分恢复",
    "禁止进行力竭训练和超高次数训练",
    "每个新动作由教练或视频确认动作模式正确后再增加重量",
]

# === 伤病对应修改方案 ===
INJURY_ADJUSTMENTS = {
    "膝盖": {
        "avoid": ["大重量深蹲", "腿举", "跳跃类动作"],
        "replace_with": ["箱式深蹲（浅蹲）", "臀桥", "腿弯举"],
        "note": "避免膝关节超过脚尖的深蹲动作，减少膝关节剪切力",
    },
    "腰部": {
        "avoid": ["传统硬拉", "早安式体前屈", "仰卧起坐"],
        "replace_with": ["平板支撑", "鸟狗式", "臀桥"],
        "note": "加强核心稳定性训练，避免腰椎屈曲和旋转负荷",
    },
    "肩部": {
        "avoid": ["颈后推举", "直立划船", "侧平举（大重量）"],
        "replace_with": ["前平举", "弹力带肩外旋", "墙壁天使"],
        "note": "避免肩关节过度外展和外旋，优先强化肩袖肌群",
    },
    "手腕": {
        "avoid": ["杠铃弯举", "窄握推举", "倒立"],
        "replace_with": ["锤式弯举", "弹力带训练", "用护腕辅助"],
        "note": "使用中性握法减少手腕压力，必要时使用护腕",
    },
}
