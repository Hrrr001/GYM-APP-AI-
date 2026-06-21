from fastapi import APIRouter, HTTPException
from models.schemas import FeedbackRequest, FeedbackResponse
from prompts import build_feedback_adjust_prompt
from llm_client import call_deepseek, extract_json_from_response
from agent.feedback_loop import FeedbackLoop
from agent.plan_adjuster import PlanAdjuster

router = APIRouter()

_feedback_history: list = []


@router.post("/feedback/submit", response_model=FeedbackResponse)
async def submit_feedback(req: FeedbackRequest):
    parsed = FeedbackLoop.parse_user_feedback(req.feeling + " " + (req.issues or ""))

    if req.issues:
        pain_fb = FeedbackLoop.parse_user_feedback(req.issues)
        parsed["pain_areas"].extend(pain_fb["pain_areas"])
        parsed["pain_areas"] = list(set(parsed["pain_areas"]))

    entry = {
        "date": req.date,
        "feeling": req.feeling,
        "issues": req.issues or "无",
        "completion_status": req.completion_status,
        "parsed": parsed,
    }
    _feedback_history.append(entry)

    return FeedbackResponse(
        parsed=parsed,
        rule_based_adjustments=[],
        ai_adjustment=None,
        follow_up_suggestion="已收到你的反馈，训练计划将在下次生成时根据反馈自动调整。",
    )


@router.post("/feedback/adjust-plan")
async def adjust_plan(req: dict):
    current_plan = req.get("current_plan", {})
    feedback_text = req.get("feedback_text", "")
    user_profile = req.get("user_profile", {})

    if not current_plan or not feedback_text:
        raise HTTPException(status_code=400, detail="请提供 current_plan 和 feedback_text")

    feedback = FeedbackLoop.parse_user_feedback(feedback_text)
    history_text = FeedbackLoop.summarize_history(_feedback_history)

    adjust_result = PlanAdjuster.rule_based_adjust(current_plan, feedback, user_profile)

    prompt = build_feedback_adjust_prompt(
        user_profile=user_profile,
        current_plan=current_plan,
        feedback={
            "date": req.get("date", ""),
            "feeling": feedback_text,
            "issues": ", ".join(feedback.get("pain_areas", [])),
            "completion_status": feedback.get("completion_estimate", "full"),
        },
        feedback_history=[{"date": h["date"], "feeling": h["feeling"],
                          "issues": h["issues"], "completion_status": h["completion_status"]}
                         for h in _feedback_history[-5:]],
    )

    llm_result = await call_deepseek(
        "你是一位专业健身教练，请根据用户反馈调整训练计划。",
        prompt, temperature=0.5, max_tokens=4096,
    )
    ai_plan = extract_json_from_response(llm_result["content"])

    return {
        "rule_based_adjustments": adjust_result["adjustments"],
        "ai_adjustment": ai_plan,
        "follow_up_suggestion": "调整后的计划已生成。请关注调整后的动作，如有不适应立即停止并咨询专业人士。",
    }
