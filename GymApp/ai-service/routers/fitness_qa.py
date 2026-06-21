from fastapi import APIRouter, HTTPException
from models.schemas import FitnessQARequest, FitnessQAResponse
from prompts import build_fitness_qa_prompt
from llm_client import call_deepseek, extract_json_from_response
from rag.knowledge_loader import KnowledgeLoader
from agent.conversation_manager import ConversationManager

router = APIRouter()

_loader: KnowledgeLoader = None
_conversation_manager = ConversationManager()


def get_loader() -> KnowledgeLoader:
    global _loader
    if _loader is None:
        _loader = KnowledgeLoader()
        _loader.initialize()
    return _loader


@router.post("/qa/ask", response_model=FitnessQAResponse)
async def ask_fitness_question(req: FitnessQARequest):
    loader = get_loader()
    retriever = loader.get_retriever()

    session = _conversation_manager.get_or_create(req.session_id, user_id=req.user_id)
    graphrag_result = retriever.retrieve_for_qa(question=req.question)
    conversation_context = session.context_for_llm(max_turns=8)

    prompt = build_fitness_qa_prompt(
        question=req.question,
        user_profile=session.user_profile,
        graphrag_context=graphrag_result.get("context_text", ""),
    )

    system_prompt = (
        "你是一位持有 NSCA-CSCS 和 ACSM 认证的专业健身教练。"
        f"以下是用户之前的对话记录：\n{conversation_context}\n\n"
        "请结合对话上下文和知识图谱检索结果，给出专业、安全、具体的回答。"
    )

    llm_result = await call_deepseek(system_prompt, prompt, temperature=0.7, max_tokens=2048)
    parsed = extract_json_from_response(llm_result["content"])

    if parsed:
        answer = parsed.get("answer", llm_result["content"])
        references = parsed.get("references", [])
        confidence = parsed.get("confidence", "中")
        safety_warnings = parsed.get("safety_warnings", [])
        related_questions = parsed.get("related_questions", [])
        disclaimer = parsed.get("disclaimer")
    else:
        answer = llm_result["content"]
        references = [n["id"] for n in graphrag_result.get("subgraph_nodes", [])]
        confidence = "中"
        safety_warnings = []
        related_questions = []
        disclaimer = None

    _conversation_manager.add_user_message(req.session_id, req.question)
    _conversation_manager.add_assistant_message(
        req.session_id, answer,
        references=references,
        safety_warnings=safety_warnings,
    )

    return FitnessQAResponse(
        answer=answer,
        references=references,
        confidence=confidence,
        safety_warnings=safety_warnings,
        related_questions=related_questions,
        disclaimer=disclaimer,
    )


@router.post("/qa/session/new")
async def create_session(user_id: int | None = None):
    import uuid
    session_id = str(uuid.uuid4())[:12]
    session = _conversation_manager.create_session(session_id, user_id=user_id)
    return {"session_id": session_id, "message": "会话创建成功"}


@router.get("/qa/session/{session_id}")
async def get_session(session_id: str):
    session = _conversation_manager.get_session(session_id)
    if session is None:
        raise HTTPException(status_code=404, detail="会话不存在")
    return session.to_dict()


@router.get("/qa/sessions")
async def list_sessions(user_id: int | None = None):
    return _conversation_manager.list_sessions(user_id=user_id)
