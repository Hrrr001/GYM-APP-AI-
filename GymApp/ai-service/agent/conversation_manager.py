from dataclasses import dataclass, field
from typing import Dict, List, Optional
from datetime import datetime


@dataclass
class ConversationTurn:
    """单轮对话记录"""
    role: str  # user / assistant
    content: str
    timestamp: str = field(default_factory=lambda: datetime.now().isoformat())
    references: List[str] = field(default_factory=list)
    safety_warnings: List[str] = field(default_factory=list)


@dataclass
class ConversationSession:
    """多轮对话会话"""
    session_id: str
    user_id: Optional[int] = None
    user_profile: Dict = field(default_factory=dict)
    turns: List[ConversationTurn] = field(default_factory=list)
    created_at: str = field(default_factory=lambda: datetime.now().isoformat())
    last_active: str = field(default_factory=lambda: datetime.now().isoformat())

    def add_turn(self, role: str, content: str, **kwargs):
        self.turns.append(ConversationTurn(role=role, content=content, **kwargs))
        self.last_active = datetime.now().isoformat()

    def context_for_llm(self, max_turns: int = 10) -> str:
        """构建送给 LLM 的对话上下文"""
        recent = self.turns[-max_turns:] if len(self.turns) > max_turns else self.turns
        lines = []
        for t in recent:
            role_label = "用户" if t.role == "user" else "AI教练"
            lines.append(f"**{role_label}** ({t.timestamp[:16]}): {t.content}")
        return "\n".join(lines)

    def to_dict(self) -> Dict:
        return {
            "session_id": self.session_id,
            "user_id": self.user_id,
            "user_profile": self.user_profile,
            "turns": [
                {
                    "role": t.role,
                    "content": t.content,
                    "timestamp": t.timestamp,
                    "references": t.references,
                    "safety_warnings": t.safety_warnings,
                }
                for t in self.turns
            ],
            "created_at": self.created_at,
            "last_active": self.last_active,
            "turn_count": len(self.turns),
        }


class ConversationManager:
    """多轮对话管理器 — 会话状态管理"""

    def __init__(self):
        self._sessions: Dict[str, ConversationSession] = {}

    def create_session(self, session_id: str, user_id: Optional[int] = None,
                       user_profile: Optional[Dict] = None) -> ConversationSession:
        session = ConversationSession(
            session_id=session_id,
            user_id=user_id,
            user_profile=user_profile or {},
        )
        self._sessions[session_id] = session
        return session

    def get_session(self, session_id: str) -> Optional[ConversationSession]:
        return self._sessions.get(session_id)

    def get_or_create(self, session_id: str, user_id: Optional[int] = None,
                      user_profile: Optional[Dict] = None) -> ConversationSession:
        if session_id in self._sessions:
            return self._sessions[session_id]
        return self.create_session(session_id, user_id, user_profile)

    def add_user_message(self, session_id: str, content: str) -> ConversationSession:
        session = self.get_or_create(session_id)
        session.add_turn("user", content)
        return session

    def add_assistant_message(self, session_id: str, content: str,
                              references: Optional[List[str]] = None,
                              safety_warnings: Optional[List[str]] = None) -> ConversationSession:
        session = self.get_or_create(session_id)
        session.add_turn("assistant", content,
                         references=references or [],
                         safety_warnings=safety_warnings or [])
        return session

    def list_sessions(self, user_id: Optional[int] = None) -> List[Dict]:
        result = []
        for s in self._sessions.values():
            if user_id is None or s.user_id == user_id:
                result.append({
                    "session_id": s.session_id,
                    "user_id": s.user_id,
                    "turn_count": len(s.turns),
                    "created_at": s.created_at,
                    "last_active": s.last_active,
                })
        result.sort(key=lambda x: x["last_active"], reverse=True)
        return result

    def delete_session(self, session_id: str) -> bool:
        if session_id in self._sessions:
            del self._sessions[session_id]
            return True
        return False
