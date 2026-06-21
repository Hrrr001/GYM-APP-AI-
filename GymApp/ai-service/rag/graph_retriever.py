import numpy as np
import networkx as nx
from typing import Dict, List, Optional, Set, Tuple
from collections import deque
from .graph_builder import FitnessKnowledgeGraph
from config import GRAPH_SIMILARITY_THRESHOLD, GRAPH_MAX_HOPS


class GraphRAGRetriever:
    """GraphRAG 检索器 — 语义匹配 + 多跳图遍历"""

    def __init__(self, kg: FitnessKnowledgeGraph):
        self.kg = kg
        self.similarity_threshold = GRAPH_SIMILARITY_THRESHOLD
        self.max_hops = GRAPH_MAX_HOPS

    def retrieve(self, query: str, k: int = 5,
                 category_filter: Optional[str] = None,
                 difficulty: Optional[str] = None) -> Dict:
        """
        GraphRAG 检索主流程：
        1. 语义相似度匹配初始种子节点
        2. 多跳图遍历扩展子图
        3. 提取子图上下文
        """
        seed_nodes = self._semantic_match(query, top_k=k)
        subgraph_nodes, subgraph_edges = self._multi_hop_expand(
            seed_nodes, max_hops=self.max_hops,
            category_filter=category_filter, difficulty=difficulty,
        )
        nodes_data = self._extract_nodes_data(subgraph_nodes)
        paths = self._find_key_paths(seed_nodes, subgraph_nodes)

        return {
            "seed_nodes": seed_nodes,
            "subgraph_nodes": nodes_data,
            "total_nodes": len(subgraph_nodes),
            "total_edges": len(subgraph_edges),
            "key_paths": paths,
            "context_text": self._serialize_context(nodes_data, paths),
        }

    def _semantic_match(self, query: str, top_k: int = 10) -> List[Dict]:
        """语义相似度匹配初始节点（TF-IDF 余弦相似度）"""
        query_embedding = self.kg.embedder.encode_single(query)
        scores = []
        for node_id, emb in self.kg._node_embeddings.items():
            if not self.kg.graph.nodes[node_id].get("content", "").strip():
                continue
            sim = float(np.dot(query_embedding, emb) /
                        (np.linalg.norm(query_embedding) * np.linalg.norm(emb) + 1e-8))
            if sim >= self.similarity_threshold:
                scores.append({"node_id": node_id, "score": round(sim, 4)})
        scores.sort(key=lambda x: x["score"], reverse=True)
        return scores[:top_k]

    def _multi_hop_expand(self, seed_nodes: List[Dict], max_hops: int,
                          category_filter: Optional[str] = None,
                          difficulty: Optional[str] = None) -> Tuple[Set[str], List[Tuple]]:
        """BFS 多跳扩展，收集子图"""
        visited = set()
        edges = []
        queue = deque()
        for seed in seed_nodes:
            sid = seed["node_id"]
            visited.add(sid)
            queue.append((sid, 0, seed["score"]))

        while queue:
            node_id, hop, score = queue.popleft()
            if hop >= max_hops:
                continue

            for _, neighbor, edge_data in self.kg.graph.out_edges(node_id, data=True):
                edge_weight = edge_data.get("weight", 0.5)
                decayed_score = score * edge_weight
                if not self._pass_filter(neighbor, category_filter, difficulty):
                    continue
                edges.append((node_id, neighbor, dict(edge_data), round(decayed_score, 4)))
                if neighbor not in visited and decayed_score >= self.similarity_threshold * 0.5:
                    visited.add(neighbor)
                    queue.append((neighbor, hop + 1, decayed_score))

            for predecessor, _, edge_data in self.kg.graph.in_edges(node_id, data=True):
                edge_weight = edge_data.get("weight", 0.5)
                decayed_score = score * edge_weight * 0.8
                if not self._pass_filter(predecessor, category_filter, difficulty):
                    continue
                edges.append((predecessor, node_id, dict(edge_data), round(decayed_score, 4)))
                if predecessor not in visited and decayed_score >= self.similarity_threshold * 0.5:
                    visited.add(predecessor)
                    queue.append((predecessor, hop + 1, decayed_score))

        return visited, edges

    def _pass_filter(self, node_id: str, category_filter: Optional[str],
                     difficulty: Optional[str]) -> bool:
        node_data = self.kg.graph.nodes.get(node_id, {})
        if category_filter and node_data.get("category") != category_filter:
            return False
        if difficulty and node_data.get("difficulty") != difficulty:
            return False
        return True

    def _extract_nodes_data(self, node_ids: Set[str]) -> List[Dict]:
        result = []
        for nid in node_ids:
            data = self.kg.graph.nodes.get(nid, {})
            result.append({
                "id": nid,
                "category": data.get("category", ""),
                "subcategory": data.get("subcategory", ""),
                "content": data.get("content", ""),
                "tags": data.get("tags", []),
                "difficulty": data.get("difficulty", ""),
                "target_muscle": data.get("target_muscle", ""),
                "source": data.get("source", ""),
            })
        return result

    def _find_key_paths(self, seed_nodes: List[Dict], all_nodes: Set[str]) -> List[Dict]:
        seed_ids = {s["node_id"] for s in seed_nodes}
        paths = []
        for sid in seed_ids:
            for tid in seed_ids:
                if sid >= tid:
                    continue
                try:
                    path = nx.shortest_path(self.kg.graph, sid, tid)
                    if len(path) <= 4:
                        path_edges = []
                        for i in range(len(path) - 1):
                            edge_data = self.kg.graph.edges[path[i], path[i + 1]]
                            path_edges.append({
                                "from": path[i],
                                "to": path[i + 1],
                                "relation": edge_data.get("relation", "unknown"),
                            })
                        paths.append({"path": path, "edges": path_edges, "length": len(path)})
                except nx.NetworkXNoPath:
                    pass
        paths.sort(key=lambda p: p["length"])
        return paths[:10]

    def _serialize_context(self, nodes_data: List[Dict], paths: List[Dict]) -> str:
        by_category = {}
        for n in nodes_data:
            cat = n.get("category", "其他")
            if cat not in by_category:
                by_category[cat] = []
            by_category[cat].append(n)

        parts = ["## 知识图谱检索结果\n"]
        for cat, nodes in by_category.items():
            parts.append(f"### {cat}")
            for n in nodes:
                parts.append(f"- [{n['id']}] {n['content']}")
                if n.get("target_muscle"):
                    parts.append(f"  目标肌群: {n['target_muscle']}")
                if n.get("difficulty"):
                    parts.append(f"  难度: {n['difficulty']}")
                if n.get("source"):
                    parts.append(f"  来源: {n['source']}")
            parts.append("")

        if paths:
            parts.append("### 知识推理路径")
            for p in paths[:5]:
                path_desc = " → ".join(
                    f"[{p['edges'][i]['from']}] --{p['edges'][i]['relation']}--> [{p['edges'][i]['to']}]"
                    for i in range(len(p['edges']))
                )
                parts.append(f"- {path_desc}")

        return "\n".join(parts)

    def retrieve_for_plan_generation(self, goal: str, injuries: Optional[str] = None,
                                     fitness_level: str = "beginner") -> Dict:
        query = f"{goal}训练计划 {fitness_level}水平"
        if injuries:
            query += f" 注意{injuries}"
        result = self.retrieve(query, k=8)
        safety_result = self.retrieve(
            f"{goal} {injuries or ''} 安全注意事项 动作禁忌",
            k=3, category_filter="安全准则"
        )
        result["safety_nodes"] = safety_result.get("subgraph_nodes", [])
        return result

    def retrieve_for_qa(self, question: str, user_level: str = "beginner") -> Dict:
        return self.retrieve(
            question, k=5, difficulty=user_level if user_level != "advanced" else None
        )
