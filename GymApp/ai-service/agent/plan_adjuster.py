from typing import Dict, List, Optional
from prompts.safety_constraints import INJURY_ADJUSTMENTS, BEGINNER_SAFETY_RULES


class PlanAdjuster:
    """训练计划调整器 — 规则驱动的安全修正"""

    @classmethod
    def rule_based_adjust(cls, plan: Dict, feedback: Dict,
                          user_profile: Optional[Dict] = None) -> Dict:
        """
        基于反馈规则修正训练计划。
        - 检测疼痛 → 替换高风险动作
        - 强度不适 → 调整组/次/重量
        - 新手保护 → 施加额外约束
        """
        adjustments = []
        adjusted_plan = cls._deep_copy_plan(plan)

        # 1. 伤病检测 → 动作替换
        pain_areas = feedback.get("pain_areas", [])
        if pain_areas:
            injury_adjustments = cls._apply_injury_adjustments(
                adjusted_plan, pain_areas
            )
            adjustments.extend(injury_adjustments)

        # 2. 强度调整
        intensity = feedback.get("intensity_signal")
        if intensity:
            vol_adjustments = cls._apply_intensity_adjustment(
                adjusted_plan, intensity
            )
            adjustments.extend(vol_adjustments)

        # 3. 新手安全约束
        fitness_level = (user_profile or {}).get("fitness_level", "")
        training_years = float((user_profile or {}).get("training_years", 0))
        if fitness_level == "beginner" or training_years < 0.5:
            beginner_notes = cls._apply_beginner_safety(adjusted_plan)
            adjustments.extend(beginner_notes)

        return {
            "adjusted_plan": adjusted_plan,
            "adjustments": adjustments,
            "adjustment_count": len(adjustments),
        }

    @classmethod
    def _deep_copy_plan(cls, plan: Dict) -> Dict:
        import copy
        return copy.deepcopy(plan)

    @classmethod
    def _apply_injury_adjustments(cls, plan: Dict, pain_areas: List[str]) -> List[Dict]:
        """根据疼痛部位替换训练动作"""
        adjustments = []
        weekly_plans = plan.get("weekly_plans", [])

        for week in weekly_plans:
            for day in week.get("days", []):
                for exercise in day.get("exercises", []):
                    ex_name = exercise.get("name", "")
                    for area in pain_areas:
                        injury_info = INJURY_ADJUSTMENTS.get(cls._cn_area(area), {})
                        avoid_list = injury_info.get("avoid", [])
                        replace_list = injury_info.get("replace_with", [])
                        for i, avoid_ex in enumerate(avoid_list):
                            if avoid_ex in ex_name:
                                replacement = replace_list[i % len(replace_list)] if replace_list else "替代动作"
                                adjustments.append({
                                    "type": "动作替换",
                                    "original": ex_name,
                                    "adjusted": replacement,
                                    "reason": f"用户报告{area}不适，避免{avoid_ex}类动作",
                                })
                                exercise["name"] = replacement
                                exercise["notes"] = (exercise.get("notes", "") +
                                    f" [因{area}不适调整] {injury_info.get('note', '')}")
        return adjustments

    @classmethod
    def _cn_area(cls, en_area: str) -> str:
        """英文疼痛区域转中文映射键"""
        mapping = {
            "knee": "膝盖", "waist": "腰部", "shoulder": "肩部", "wrist": "手腕",
            "elbow": "肘部", "ankle": "脚踝", "hip": "髋部", "neck": "脖子",
        }
        return mapping.get(en_area, en_area)

    @classmethod
    def _apply_intensity_adjustment(cls, plan: Dict, intensity: str) -> List[Dict]:
        """根据强度反馈调整训练量"""
        adjustments = []
        weekly_plans = plan.get("weekly_plans", [])

        factor = 0.8 if intensity == "too_hard" else 1.1  # 减轻/增加

        for week in weekly_plans:
            for day in week.get("days", []):
                for exercise in day.get("exercises", []):
                    if exercise.get("sets") and isinstance(exercise["sets"], int):
                        old_sets = exercise["sets"]
                        new_sets = max(1, round(old_sets * factor))
                        if new_sets != old_sets:
                            adjustments.append({
                                "type": "强度调整",
                                "original": f"sets={old_sets}",
                                "adjusted": f"sets={new_sets}",
                                "reason": f"用户反馈强度{'过大' if intensity == 'too_hard' else '不足'}",
                            })
                            exercise["sets"] = new_sets

                    if exercise.get("reps") and isinstance(exercise["reps"], str):
                        original_reps = exercise["reps"]
                        exercise["reps"] = cls._scale_rep_range(exercise["reps"], factor)
                        if exercise["reps"] != original_reps:
                            adjustments.append({
                                "type": "次数调整",
                                "original": original_reps,
                                "adjusted": exercise["reps"],
                                "reason": "匹配用户当前能力水平",
                            })
        return adjustments

    @classmethod
    def _scale_rep_range(cls, reps: str, factor: float) -> str:
        """缩放次数范围，如 '12-15' * 0.8 → '10-12'"""
        import re
        nums = re.findall(r"\d+", reps)
        if len(nums) >= 2:
            lo = max(1, round(int(nums[0]) * factor))
            hi = max(lo + 1, round(int(nums[-1]) * factor))
            return f"{lo}-{hi}"
        return reps

    @classmethod
    def _apply_beginner_safety(cls, plan: Dict) -> List[Dict]:
        """应用初学者安全约束"""
        notes = []
        weekly_plans = plan.get("weekly_plans", [])
        for week in weekly_plans:
            for day in week.get("days", []):
                for exercise in day.get("exercises", []):
                    ex_name = exercise.get("name", "")
                    # 禁止大重量复合动作
                    forbidden = ["大重量", "极限", "力竭"]
                    if any(f in ex_name for f in forbidden):
                        exercise["name"] = ex_name.replace("大重量", "轻重量")
                        exercise["notes"] = exercise.get("notes", "") + " [新手安全约束：避免极限重量训练]"
                        notes.append({
                            "type": "安全约束",
                            "original": ex_name,
                            "adjusted": exercise["name"],
                            "reason": "新手应避免极限重量训练，优先建立正确动作模式",
                        })
        if not notes:
            notes.append({
                "type": "安全约束",
                "original": "—",
                "adjusted": "已确认",
                "reason": "当前计划符合新手安全准则",
            })
        return notes
