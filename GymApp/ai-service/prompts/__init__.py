"""Prompt 构建器 — 组合角色设定、安全约束、格式规范，生成完整 Prompt"""
from .templates import (
    SYSTEM_ROLE,
    SAFETY_CONSTRAINTS,
    TRAINING_PLAN_TEMPLATE,
    FITNESS_QA_TEMPLATE,
    FEEDBACK_ADJUST_TEMPLATE,
)
from .format_specs import TRAINING_PLAN_FORMAT, FITNESS_ANSWER_FORMAT, PLAN_ADJUST_FORMAT
import json
from typing import Dict, List


def build_training_plan_prompt(user_data: Dict) -> str:
    """构建训练计划生成的完整 Prompt"""
    return TRAINING_PLAN_TEMPLATE.format(
        system_role=SYSTEM_ROLE,
        safety_constraints=SAFETY_CONSTRAINTS,
        gender=user_data.get("gender", "未提供"),
        age=user_data.get("age", "未提供"),
        height=user_data.get("height", "未提供"),
        weight=user_data.get("weight", "未提供"),
        body_fat=user_data.get("body_fat", "未提供"),
        training_years=user_data.get("training_years", "未提供"),
        fitness_level=user_data.get("fitness_level", "beginner"),
        injuries=user_data.get("injuries", "无"),
        equipment=user_data.get("equipment", "未提供"),
        goal=user_data.get("goal", "塑形"),
        duration=user_data.get("duration", 4),
        days_per_week=user_data.get("days_per_week", 3),
        session_minutes=user_data.get("session_minutes", 60),
        additional_requirements=user_data.get("additional_requirements", "无"),
        format_spec=json.dumps(TRAINING_PLAN_FORMAT, ensure_ascii=False, indent=2),
    )


def build_fitness_qa_prompt(question: str, user_profile: Dict, graphrag_context: str) -> str:
    """构建健身咨询的 Prompt（含 GraphRAG 知识图谱检索结果）"""
    return FITNESS_QA_TEMPLATE.format(
        system_role=SYSTEM_ROLE,
        safety_constraints=SAFETY_CONSTRAINTS,
        user_profile=json.dumps(user_profile, ensure_ascii=False, indent=2),
        graphrag_context=graphrag_context or "未找到直接相关的专业知识，请基于通用健身知识回答，并建议用户咨询专业教练。",
        question=question,
    )


def build_feedback_adjust_prompt(
    user_profile: Dict, current_plan: Dict, feedback: Dict, feedback_history: List[Dict]
) -> str:
    """构建训练反馈调整的 Prompt"""
    return FEEDBACK_ADJUST_TEMPLATE.format(
        system_role=SYSTEM_ROLE,
        safety_constraints=SAFETY_CONSTRAINTS,
        user_profile=json.dumps(user_profile, ensure_ascii=False, indent=2),
        current_plan=json.dumps(current_plan, ensure_ascii=False, indent=2),
        feedback_date=feedback.get("date", "未提供"),
        feeling=feedback.get("feeling", "未提供"),
        issues=feedback.get("issues", "无"),
        completion_status=feedback.get("completion_status", "未提供"),
        feedback_history=json.dumps(feedback_history, ensure_ascii=False, indent=2),
        format_spec=json.dumps(PLAN_ADJUST_FORMAT, ensure_ascii=False, indent=2),
    )
