import re
from typing import Dict, List, Optional


class FeedbackLoop:
    """训练反馈闭环 — 解析用户反馈，汇总历史，驱动计划调整"""

    PAIN_KEYWORDS = {
        "膝盖": "knee",
        "膝": "knee",
        "腰": "waist",
        "下背": "waist",
        "腰椎": "waist",
        "肩膀": "shoulder",
        "肩": "shoulder",
        "肩袖": "shoulder",
        "手腕": "wrist",
        "腕": "wrist",
        "肘": "elbow",
        "肘部": "elbow",
        "脚踝": "ankle",
        "踝": "ankle",
        "髋": "hip",
        "髋部": "hip",
        "脖子": "neck",
        "颈": "neck",
    }

    INTENSITY_SIGNALS = {
        "太轻松": "too_easy",
        "不够": "too_easy",
        "没感觉": "too_easy",
        "可以加": "too_easy",
        "太累": "too_hard",
        "受不了": "too_hard",
        "太重": "too_hard",
        "恢复不过来": "too_hard",
        "酸痛": "too_hard",
        "刚好": "just_right",
        "合适": "just_right",
        "不错": "just_right",
    }

    @classmethod
    def parse_user_feedback(cls, feedback_text: str) -> Dict:
        """
        从用户自由文本反馈中解析结构化信息。
        返回: {
            "pain_areas": [...],
            "intensity_signal": "too_easy"|"too_hard"|"just_right"|null,
            "completion_estimate": "full"|"partial"|"missed",
            "key_phrases": [...],
        }
        """
        result = {
            "pain_areas": [],
            "intensity_signal": None,
            "completion_estimate": "full",
            "key_phrases": [],
        }

        text_lower = feedback_text.lower()

        # 伤病/疼痛检测
        for cn, en in cls.PAIN_KEYWORDS.items():
            if cn in feedback_text:
                result["pain_areas"].append(en)
        result["pain_areas"] = list(set(result["pain_areas"]))

        # 强度信号
        for phrase, signal in cls.INTENSITY_SIGNALS.items():
            if phrase in feedback_text:
                result["intensity_signal"] = signal
                break

        # 完成情况
        missed_words = ["没做", "没练", "没完成", "跳过了", "没去", "偷懒", "没时间"]
        partial_words = ["做了一部分", "没做完", "只做了", "做了一半", "提前结束"]
        if any(w in feedback_text for w in missed_words):
            result["completion_estimate"] = "missed"
        elif any(w in feedback_text for w in partial_words):
            result["completion_estimate"] = "partial"

        # 关键短语提取
        key_phrases = re.findall(r"[一-鿿]{4,}", feedback_text)
        result["key_phrases"] = key_phrases[:5]

        return result

    @classmethod
    def summarize_history(cls, feedback_history: List[Dict], max_items: int = 5) -> str:
        """汇总历史反馈为 LLM 可读的文本"""
        if not feedback_history:
            return "无历史反馈记录"

        lines = []
        for i, fb in enumerate(feedback_history[-max_items:], 1):
            date = fb.get("date", "未知日期")
            feeling = fb.get("feeling", "")
            issues = fb.get("issues", "无")
            status = fb.get("completion_status", "")
            lines.append(f"{i}. [{date}] 感受: {feeling} | 问题: {issues} | 完成: {status}")
        return "\n".join(lines)
