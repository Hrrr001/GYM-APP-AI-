import json
import pickle
import networkx as nx
import numpy as np
import os
from typing import Dict, List, Optional, Set
from sklearn.feature_extraction.text import TfidfVectorizer
from config import GRAPH_RAG_PERSIST_DIR


class TfidfEmbedder:
    """本地 TF-IDF 向量化器，无需网络下载模型"""

    def __init__(self):
        self.vectorizer = TfidfVectorizer(max_features=1024, analyzer="char_wb", ngram_range=(2, 4))

    def fit(self, texts: List[str]):
        self.vectorizer.fit(texts)

    def encode(self, texts: List[str], **__) -> np.ndarray:
        return self.vectorizer.transform(texts).toarray().astype(np.float32)

    def encode_single(self, text: str) -> np.ndarray:
        return self.vectorizer.transform([text]).toarray().astype(np.float32)[0]


class FitnessKnowledgeGraph:
    """健身知识图谱 — 基于 NetworkX 的 GraphRAG（TF-IDF 本地嵌入）"""

    RELATION_TYPES = {
        "prerequisite": "前置动作/知识（学习A之前应先掌握B）",
        "progression_of": "进阶关系（A是B的进阶版本）",
        "alternative_to": "替代关系（A可替代B）",
        "contraindication_for": "禁忌关系（A对B情况禁忌）",
        "targets": "目标肌群（A训练B肌肉）",
        "similar_to": "相似关系（A和B动作模式相似）",
        "safety_for": "安全建议（A是B情况的安全建议）",
        "complements": "互补关系（A和B搭配训练效果好）",
    }

    def __init__(self, persist_dir: str = GRAPH_RAG_PERSIST_DIR):
        self.persist_dir = persist_dir
        self.graph = nx.DiGraph()
        self.embedder = TfidfEmbedder()
        self._node_embeddings: Dict[str, np.ndarray] = {}
        os.makedirs(persist_dir, exist_ok=True)

    def load_from_json(self, filepath: str) -> int:
        """从 JSON 文件加载知识并构建图谱"""
        with open(filepath, "r", encoding="utf-8") as f:
            knowledge_list = json.load(f)

        for item in knowledge_list:
            node_id = item["id"]
            self.graph.add_node(
                node_id,
                category=item.get("category", ""),
                subcategory=item.get("subcategory", ""),
                content=item.get("content", ""),
                tags=item.get("tags", []),
                difficulty=item.get("difficulty", ""),
                target_muscle=item.get("target_muscle", ""),
                source=item.get("source", ""),
            )

            relationships = item.get("relationships", {})
            for rel_type, target_ids in relationships.items():
                if rel_type in self.RELATION_TYPES:
                    for target_id in target_ids:
                        if not self.graph.has_node(target_id):
                            self.graph.add_node(target_id, content="", category="")
                        self.graph.add_edge(
                            node_id, target_id,
                            relation=rel_type,
                            weight=self._get_edge_weight(rel_type),
                        )

        self._compute_embeddings()
        return len(knowledge_list)

    def _get_edge_weight(self, rel_type: str) -> float:
        weights = {
            "prerequisite": 0.9, "progression_of": 0.8,
            "alternative_to": 0.7, "contraindication_for": 1.0,
            "targets": 0.4, "similar_to": 0.6,
            "safety_for": 0.95, "complements": 0.5,
        }
        return weights.get(rel_type, 0.5)

    def _compute_embeddings(self):
        texts = []
        node_ids = []
        for node_id, data in self.graph.nodes(data=True):
            text = self._node_to_text(node_id, data)
            if text.strip():
                texts.append(text)
                node_ids.append(node_id)

        if texts:
            self.embedder.fit(texts)
            embeddings = self.embedder.encode(texts)
            for i, node_id in enumerate(node_ids):
                self._node_embeddings[node_id] = embeddings[i]

    def _node_to_text(self, node_id: str, data: Dict) -> str:
        return " ".join([
            data.get("category", ""),
            data.get("subcategory", ""),
            " ".join(data.get("tags", [])),
            data.get("target_muscle", ""),
            data.get("content", ""),
        ])

    def save(self):
        graph_path = os.path.join(self.persist_dir, "knowledge_graph.gpickle")
        with open(graph_path, "wb") as f:
            pickle.dump(self.graph, f)
        emb_path = os.path.join(self.persist_dir, "node_embeddings.npz")
        np.savez(emb_path, **self._node_embeddings)

    def load(self) -> bool:
        graph_path = os.path.join(self.persist_dir, "knowledge_graph.gpickle")
        emb_path = os.path.join(self.persist_dir, "node_embeddings.npz")
        if not os.path.exists(graph_path) or not os.path.exists(emb_path):
            return False
        with open(graph_path, "rb") as f:
            self.graph = pickle.load(f)
        loaded = np.load(emb_path, allow_pickle=True)
        self._node_embeddings = {k: loaded[k] for k in loaded.files}
        # Re-fit the TF-IDF vectorizer so transform() works after load
        texts = []
        for node_id, data in self.graph.nodes(data=True):
            text = self._node_to_text(node_id, data)
            if text.strip():
                texts.append(text)
        if texts:
            self.embedder.fit(texts)
        return True

    def get_stats(self) -> Dict:
        return {
            "total_nodes": self.graph.number_of_nodes(),
            "total_edges": self.graph.number_of_edges(),
            "nodes_with_content": sum(
                1 for _, d in self.graph.nodes(data=True) if d.get("content", "").strip()
            ),
            "relation_counts": {
                rel: sum(1 for _, _, d in self.graph.edges(data=True) if d.get("relation") == rel)
                for rel in self.RELATION_TYPES
            },
        }
