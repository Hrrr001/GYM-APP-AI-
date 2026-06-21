from fastapi import APIRouter, HTTPException
from models.schemas import PlanGenerateRequest, PlanGenerateResponse
from prompts import build_training_plan_prompt
from llm_client import call_deepseek, extract_json_from_response
from rag.knowledge_loader import KnowledgeLoader

router = APIRouter()

_loader: KnowledgeLoader = None


def get_loader() -> KnowledgeLoader:
    global _loader
    if _loader is None:
        _loader = KnowledgeLoader()
        _loader.initialize()
    return _loader


@router.post("/plan/generate", response_model=PlanGenerateResponse)
async def generate_training_plan(req: PlanGenerateRequest):
    loader = get_loader()
    retriever = loader.get_retriever()

    user_data = req.model_dump()
    graphrag_result = retriever.retrieve_for_plan_generation(
        goal=req.goal,
        injuries=req.injuries,
        fitness_level=req.fitness_level,
    )

    prompt = build_training_plan_prompt(user_data)

    system_prompt = (
        "你是一位专业健身教练。请严格按照 JSON 格式输出训练计划，"
        "不要输出任何 JSON 之外的内容。确保计划符合运动科学和安全准则。"
    )
    llm_result = await call_deepseek(system_prompt, prompt, temperature=0.5, max_tokens=4096)
    plan = extract_json_from_response(llm_result["content"])

    if plan is None:
        raise HTTPException(status_code=500, detail="AI 生成的训练计划格式异常，请重试")

    safety_refs = [n["id"] for n in graphrag_result.get("subgraph_nodes", [])]

    return PlanGenerateResponse(
        plan=plan,
        safety_references_used=safety_refs,
        prompt_tokens_used=llm_result["prompt_tokens"],
    )
