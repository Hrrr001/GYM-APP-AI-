from typing import Dict, Optional
from .graph_builder import FitnessKnowledgeGraph
from .graph_retriever import GraphRAGRetriever


class KnowledgeLoader:
    """GraphRAG 知识加载器"""

    def __init__(self):
        self.kg = FitnessKnowledgeGraph()
        self.retriever: Optional[GraphRAGRetriever] = None

    def initialize(self, knowledge_file: str = "data/fitness_knowledge.json") -> Dict:
        """一站式初始化：加载知识 → 构建图谱 → 创建检索器"""
        if self.kg.load():
            stats = self.kg.get_stats()
            self.retriever = GraphRAGRetriever(self.kg)
            return {
                "source": "persisted",
                "stats": stats,
                "message": "从磁盘加载已有知识图谱",
            }

        count = self.kg.load_from_json(knowledge_file)
        self.kg.save()
        self.retriever = GraphRAGRetriever(self.kg)
        return {
            "source": "json",
            "total_loaded": count,
            "stats": self.kg.get_stats(),
            "message": f"成功从 JSON 加载 {count} 条知识并构建知识图谱",
        }

    def get_retriever(self) -> GraphRAGRetriever:
        if self.retriever is None:
            raise RuntimeError("知识图谱未初始化，请先调用 initialize()")
        return self.retriever

    def get_stats(self) -> Dict:
        return self.kg.get_stats()
