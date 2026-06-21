from pydantic import BaseModel
from typing import Optional, List, Dict, Any


class PlanGenerateRequest(BaseModel):
    """训练计划生成请求"""
    gender: str
    age: int
    height: float
    weight: float
    body_fat: Optional[float] = None
    training_years: float
    fitness_level: str  # beginner / intermediate / advanced
    injuries: Optional[str] = None
    equipment: Optional[str] = None
    goal: str  # 增肌 / 减脂 / 塑形 / 力量提升 / 耐力增强
    duration: int  # 周
    days_per_week: int = 3
    session_minutes: int = 60
    additional_requirements: Optional[str] = None


class PlanGenerateResponse(BaseModel):
    """训练计划生成响应"""
    plan: Dict[str, Any]
    safety_references_used: List[str] = []
    prompt_tokens_used: int = 0


class FitnessQARequest(BaseModel):
    """健身咨询请求"""
    session_id: str
    question: str
    user_id: Optional[int] = None


class FitnessQAResponse(BaseModel):
    """健身咨询响应"""
    answer: str
    references: List[str] = []
    confidence: str = "中"
    safety_warnings: List[str] = []
    related_questions: List[str] = []
    disclaimer: Optional[str] = None


class FeedbackRequest(BaseModel):
    """训练反馈请求"""
    user_id: int
    plan_id: Optional[int] = None
    date: str
    feeling: str
    issues: Optional[str] = None
    completion_status: str  # completed / partial / missed


class FeedbackResponse(BaseModel):
    """训练反馈响应"""
    parsed: Dict[str, Any]
    rule_based_adjustments: List[Dict[str, Any]] = []
    ai_adjustment: Optional[Dict[str, Any]] = None
    follow_up_suggestion: Optional[str] = None
