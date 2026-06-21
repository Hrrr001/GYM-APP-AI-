# GymSoftware AI 智能健身助手 — 全面升级方案

**Goal:** 将当前基础版健身 App 升级为简历所述的 AI 智能健身助手，实现 Prompt Engineering 体系、RAG 知识库、多轮对话 Agent 和训练反馈闭环。

**Architecture:** 新增 Python/FastAPI AI 微服务层，负责所有 AI 相关功能（Prompt 模板、GraphRAG 知识图谱检索、多轮对话），与现有 Java/Spring Boot CRUD 后端通过 REST API 通信。前端 Flutter 增加健身咨询对话界面和训练反馈入口。

## Global Constraints
- Java 后端保持 Spring Boot 2.7.18，不做框架迁移
- Python 3.10+，FastAPI + LangChain + LangGraph + GraphRAG
- DeepSeek API 作为 LLM 后端
- MySQL 保持不变，GraphRAG 使用 NetworkX 构建知识图谱 + sentence-transformers embedding
- 所有 AI 功能走 Python 微服务，Java 后端做数据 CRUD 和业务路由

---

## 文件结构设计

### 新增文件 (Python AI 微服务)
```
GymApp/ai-service/
├── requirements.txt
├── main.py                          # FastAPI 入口
├── config.py                        # 配置管理
├── prompts/
│   ├── __init__.py
│   ├── templates.py                 # Prompt 模板体系
│   ├── safety_constraints.py        # 安全约束规则
│   └── format_specs.py              # 输出格式规范
├── rag/
│   ├── __init__.py
│   ├── knowledge_loader.py          # 知识片段加载器
│   ├── vector_store.py              # ChromaDB 向量存储
│   └── retriever.py                 # 语义检索器
├── agent/
│   ├── __init__.py
│   ├── conversation_manager.py      # 多轮对话状态管理
│   ├── feedback_loop.py             # 训练反馈闭环
│   └── plan_adjuster.py             # 计划自动调整
├── routers/
│   ├── __init__.py
│   ├── plan_generation.py           # 训练计划生成 API
│   ├── fitness_qa.py                # 健身咨询 API
│   └── feedback.py                  # 训练反馈 API
├── models/
│   ├── __init__.py
│   └── schemas.py                   # Pydantic 数据模型
└── data/
    └── fitness_knowledge.json       # 500+ 条健身知识片段
```

### 修改文件 (Java 后端)
```
GymApp/backend/src/main/java/com/gym/
├── service/AIService.java           # 改为调用 Python AI 服务
├── controller/AIController.java     # 新增多轮对话、反馈相关端点
├── controller/TrainingPlanController.java  # 增强 AI 计划生成
└── application.properties           # 新增 AI 服务地址配置
```

### 修改文件 (Flutter 前端)
```
GymApp/frontend/lib/
├── screens/ai_assistant_screen.dart       # 升级为多轮对话界面
├── screens/ai_plan_generate_screen.dart   # 升级为带体测数据的计划生成
├── screens/workout_checkin_screen.dart    # 增加训练反馈入口
└── services/ai_service.dart               # 新增 AI API 调用服务
```

---

## 任务列表

### Phase 1: Python AI 微服务基础搭建

#### Task 1.1: 创建 Python 项目骨架
**文件:** `GymApp/ai-service/requirements.txt`, `GymApp/ai-service/main.py`, `GymApp/ai-service/config.py`

- [ ] 创建 requirements.txt 包含所有依赖
- [ ] 创建 FastAPI 主入口 main.py
- [ ] 创建配置管理 config.py
- [ ] 验证服务可启动

```python
# requirements.txt
fastapi==0.104.1
uvicorn==0.24.0
langchain==0.1.0
langchain-community==0.1.0
chromadb==0.4.22
pydantic==2.5.0
httpx==0.25.0
python-dotenv==1.0.0
sentence-transformers==2.2.2
```

```python
# config.py
import os
from dotenv import load_dotenv

load_dotenv()

DEEPSEEK_API_KEY = os.getenv("DEEPSEEK_API_KEY", "")
DEEPSEEK_API_URL = "https://api.deepseek.com/v1/chat/completions"
DEEPSEEK_MODEL = "deepseek-chat"

CHROMA_PERSIST_DIR = os.getenv("CHROMA_PERSIST_DIR", "./chroma_db")
EMBEDDING_MODEL = os.getenv("EMBEDDING_MODEL", "shibing624/text2vec-base-chinese")

MYSQL_HOST = os.getenv("MYSQL_HOST", "localhost")
MYSQL_PORT = int(os.getenv("MYSQL_PORT", "3306"))
MYSQL_USER = os.getenv("MYSQL_USER", "root")
MYSQL_PASSWORD = os.getenv("MYSQL_PASSWORD", "")
MYSQL_DATABASE = os.getenv("MYSQL_DATABASE", "gym_app")

AI_SERVICE_PORT = int(os.getenv("AI_SERVICE_PORT", "8000"))
```

```python
# main.py
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from routers import plan_generation, fitness_qa, feedback

app = FastAPI(title="Gym AI Service", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(plan_generation.router, prefix="/api/ai", tags=["plan"])
app.include_router(fitness_qa.router, prefix="/api/ai", tags=["qa"])
app.include_router(feedback.router, prefix="/api/ai", tags=["feedback"])

@app.get("/health")
async def health():
    return {"status": "ok"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
```

- [ ] `cd GymApp/ai-service && pip install -r requirements.txt`
- [ ] `python main.py` 验证服务在 8000 端口启动

---

### Phase 2: Prompt Engineering 体系

#### Task 2.1: 设计健身领域专用 Prompt 模板体系
**文件:** `GymApp/ai-service/prompts/templates.py`

- [ ] 创建角色设定模板 (SYSTEM_ROLE)
- [ ] 创建不同目标的训练计划模板 (TRAINING_PLAN_TEMPLATES)
- [ ] 创建健身咨询模板 (FITNESS_QA_TEMPLATE)
- [ ] 创建计划调整模板 (PLAN_ADJUST_TEMPLATE)

```python
# prompts/templates.py

# === 角色设定 ===
SYSTEM_ROLE = """你是一位拥有10年经验的专业健身教练和运动医学顾问。你持有 NSCA-CSCS（美国国家体能协会认证体能训练专家）和 ACSM（美国运动医学学会）认证资质。

你的核心原则：
1. 安全第一：所有建议必须符合运动医学和安全训练准则
2. 个性化：根据用户的体测数据、训练年限和目标定制方案
3. 科学依据：建议基于运动科学和循证研究，不推荐未经证实的训练方法
4. 渐进超负荷：训练计划遵循科学的渐进超负荷原则
5. 恢复优先：充分重视休息、营养和恢复在训练中的作用"""

# === 安全约束 ===
SAFETY_CONSTRAINTS = """在生成任何训练建议时，你必须严格遵守以下安全规则：

1. 动作范围：推荐关节在生理安全范围内的活动幅度，禁止推荐过度拉伸或危险角度
2. 训练量控制：每周训练量增幅不超过10%，新手从低容量开始
3. 伤病预防：
   - 禁止推荐已被运动医学证实高受伤风险的动作
   - 如用户提及任何不适，必须建议先咨询医生
   - 下背痛、膝关节疼痛等常见问题必须给出修正方案
4. 禁忌组合：禁止建议在疲劳状态下进行高风险复合动作（如大重量深蹲+硬拉同天）
5. 热身和放松：每个训练计划必须包含热身（5-10分钟）和拉伸放松（5-10分钟）环节
6. 营养安全：不推荐极端饮食方案，每日热量缺口不超过500kcal"""

# === 训练计划 Prompt 模板 ===
TRAINING_PLAN_TEMPLATE = """{system_role}

{safety_constraints}

## 用户体测数据
- 性别：{gender}
- 年龄：{age}岁
- 身高：{height}cm
- 体重：{weight}kg
- 体脂率：{body_fat}%
- 训练年限：{training_years}年
- 健身水平：{fitness_level}
- 伤病情况：{injuries}
- 可用器械：{equipment}

## 训练目标
- 目标类型：{goal}（增肌/减脂/塑形/力量提升/耐力增强）
- 计划周期：{duration}周
- 每周训练天数：{days_per_week}天
- 每次训练时长：{session_minutes}分钟

## 额外要求
{additional_requirements}

## 输出格式要求
请按以下 JSON 格式输出训练计划（严格遵循此格式，不要输出其他内容）：

```json
{format_spec}
```

## 重要提醒
- 计划必须严格遵守安全约束中的每一条规则
- 根据用户训练年限合理分配训练量：新手以动作学习为主，中级以渐进超负荷为主，高级以专项突破为主
- 如果用户有伤病情况，必须在计划中标注替换动作和注意事项
"""

# === 健身咨询 Prompt 模板（GraphRAG 版）===
FITNESS_QA_TEMPLATE = """{system_role}

{safety_constraints}

## 用户信息
{user_profile}

## 知识图谱检索结果（基于用户问题多跳检索的相关专业知识子图）
{graphrag_context}

## 用户问题
{question}

## 回答要求
1. 优先引用知识图谱检索到的专业知识
2. 如果检索到了相关路径（如"动作A→替代→动作B→安全→动作C"），在回答中说明这些知识之间的关联
3. 如果没有相关知识支持，明确告知用户"该问题建议咨询专业教练"
4. 如果涉及潜在安全风险，必须标注警告
5. 回答要具体、可操作，避免空洞的理论
6. 如适用，提供替代方案或进阶/退阶选择
"""

# === 训练反馈闭环模板 ===
FEEDBACK_ADJUST_TEMPLATE = """{system_role}

{safety_constraints}

## 用户信息
{user_profile}

## 当前训练计划
{current_plan}

## 用户训练反馈
- 反馈日期：{feedback_date}
- 训练感受：{feeling}
- 具体不适/问题：{issues}
- 完成情况：{completion_status}

## 历史反馈记录
{feedback_history}

## 调整要求
根据用户反馈，请对训练计划做出以下调整：
1. 如果用户报告某部位不适（如膝盖、腰部），替换相关高风险动作，降低强度
2. 如果用户反映训练强度不够，在安全范围内适当增加（每周增幅≤10%）
3. 如果用户反映强度过大，降低组数、次数或重量
4. 调整后的计划必须保持与原计划相同的目标方向

## 输出格式
请按以下 JSON 格式输出调整后的计划：

```json
{format_spec}
```

## 调整说明
同时输出一段简短的文字说明，解释你做了哪些调整以及为什么。
"""
```

#### Task 2.2: 创建安全约束和输出格式规范
**文件:** `GymApp/ai-service/prompts/safety_constraints.py`, `GymApp/ai-service/prompts/format_specs.py`

- [ ] 实现安全约束规则检查函数
- [ ] 实现输出格式规范定义
- [ ] 实现 Prompt 构建器（组合角色+约束+格式）

```python
# prompts/safety_constraints.py

from typing import List, Dict, Optional

# === 高风险动作列表（运动医学共识） ===
HIGH_RISK_EXERCISES = {
    "颈后下拉": "肩关节前囊过度牵拉，易导致肩袖损伤",
    "颈后推举": "肩关节极限外旋位，易导致肩峰撞击综合征",
    "直腿仰卧起坐": "过度屈髋导致腰椎前凸，增加椎间盘压力",
    "罗马尼亚硬拉（新手）": "新手动作模式不正确易导致腰椎损伤",
    "史密斯机深蹲": "限制自然运动轨迹，增加膝关节剪切力",
    "器械腿伸展（全范围）": "膝关节终末伸展位对前交叉韧带压力过大",
    "直立划船（窄握）": "肩关节内旋+外展位，易导致肩峰撞击",
}

# === 安全规则检查 ===
def check_exercise_safety(exercise_name: str) -> Dict:
    """检查动作安全性，返回风险评估"""
    for risky_exercise, reason in HIGH_RISK_EXERCISES.items():
        if risky_exercise in exercise_name:
            return {
                "is_safe": False,
                "risk": reason,
                "recommendation": f"建议替换为更安全的替代动作，或由专业教练一对一指导"
            }
    return {"is_safe": True}

# === 训练量安全检查 ===
def check_volume_safety(current_volume: int, new_volume: int) -> Dict:
    """检查训练量增幅是否在安全范围内（≤10%）"""
    if current_volume == 0:
        return {"is_safe": True}
    increase_pct = (new_volume - current_volume) / current_volume * 100
    if increase_pct > 10:
        return {
            "is_safe": False,
            "current_volume": current_volume,
            "new_volume": new_volume,
            "increase_pct": round(increase_pct, 1),
            "reason": f"训练量增幅{increase_pct:.1f}%超过安全上限10%"
        }
    return {"is_safe": True}

# === 初学者安全约束 ===
BEGINNER_SAFETY_RULES = [
    "前三周以动作学习为主，每组15-20次轻重量，重点建立正确运动模式",
    "避免自由重量复合动作（如杠铃深蹲、硬拉），优先使用固定器械",
    "每周训练不超过3次，确保充分恢复",
    "禁止进行力竭训练和超高次数训练",
    "每个新动作由教练或视频确认动作模式正确后再增加重量",
]

# === 伤病对应修改方案 ===
INJURY_ADJUSTMENTS = {
    "膝盖": {
        "avoid": ["大重量深蹲", "腿举", "跳跃类动作"],
        "replace_with": ["箱式深蹲（浅蹲）", "臀桥", "腿弯举"],
        "note": "避免膝关节超过脚尖的深蹲动作，减少膝关节剪切力"
    },
    "腰部": {
        "avoid": ["传统硬拉", "早安式体前屈", "仰卧起坐"],
        "replace_with": ["平板支撑", "鸟狗式", "臀桥"],
        "note": "加强核心稳定性训练，避免腰椎屈曲和旋转负荷"
    },
    "肩部": {
        "avoid": ["颈后推举", "直立划船", "侧平举（大重量）"],
        "replace_with": ["前平举", "弹力带肩外旋", "墙壁天使"],
        "note": "避免肩关节过度外展和外旋，优先强化肩袖肌群"
    },
    "手腕": {
        "avoid": ["杠铃弯举", "窄握推举", "倒立"],
        "replace_with": ["锤式弯举", "弹力带训练", "用护腕辅助"],
        "note": "使用中性握法减少手腕压力，必要时使用护腕"
    },
}
```

```python
# prompts/format_specs.py

# === 训练计划输出 JSON Schema ===
TRAINING_PLAN_FORMAT = {
    "plan_name": "计划名称（如：4周增肌训练计划）",
    "plan_overview": "计划总体说明（200字以内）",
    "safety_notes": ["安全注意事项1", "安全注意事项2"],
    "warmup_routine": {
        "duration_minutes": 10,
        "exercises": [
            {"name": "动态拉伸-高抬腿", "duration": "60秒", "sets": 1},
            {"name": "关节活动-肩绕环", "duration": "30秒", "sets": 2},
        ]
    },
    "cooldown_routine": {
        "duration_minutes": 10,
        "exercises": [
            {"name": "静态拉伸-股四头肌", "duration": "30秒/侧", "sets": 2},
        ]
    },
    "weekly_plans": [
        {
            "week": 1,
            "theme": "本周训练主题（如：适应性训练，建立运动模式）",
            "days": [
                {
                    "day": 1,
                    "focus": "训练重点（如：上肢推+核心）",
                    "exercises": [
                        {
                            "name": "动作名称",
                            "sets": 3,
                            "reps": "12-15",
                            "weight": "自体重或建议重量",
                            "rest_seconds": 60,
                            "notes": "动作要点或替换动作",
                            "safety_tip": "安全提示（如有风险）"
                        }
                    ]
                }
            ]
        }
    ]
}

# === 健身咨询回答格式 ===
FITNESS_ANSWER_FORMAT = {
    "answer": "详细回答内容",
    "references": ["引用的知识库条目ID1", "引用的知识库条目ID2"],
    "confidence": "高/中/低 — 表示回答的确定程度",
    "safety_warnings": ["安全警告1", "安全警告2"],
    "related_questions": ["相关问题1", "相关问题2"],
    "disclaimer": "如有专业知识不足，注明建议咨询专业人士"
}

# === 计划调整输出格式 ===
PLAN_ADJUST_FORMAT = {
    "adjustment_summary": "调整总结",
    "adjustments_made": [
        {
            "type": "动作替换/强度调整/频率调整",
            "original": "原来的内容",
            "adjusted": "调整后的内容",
            "reason": "调整原因（关联用户反馈）"
        }
    ],
    "adjusted_plan": "{与TRAINING_PLAN_FORMAT结构相同}",
    "follow_up_questions": ["跟进问题，询问用户下一步感受"]
}
```

#### Task 2.3: 实现 Prompt 构建器
**文件:** `GymApp/ai-service/prompts/__init__.py`

- [ ] 实现 build_training_plan_prompt() 函数
- [ ] 实现 build_fitness_qa_prompt() 函数
- [ ] 实现 build_feedback_adjust_prompt() 函数
- [ ] 单元测试验证 Prompt 输出格式正确

```python
# prompts/__init__.py
from .templates import (
    SYSTEM_ROLE, SAFETY_CONSTRAINTS,
    TRAINING_PLAN_TEMPLATE, FITNESS_QA_TEMPLATE, FEEDBACK_ADJUST_TEMPLATE
)
from .format_specs import TRAINING_PLAN_FORMAT, FITNESS_ANSWER_FORMAT, PLAN_ADJUST_FORMAT
import json
from typing import Dict, List, Optional

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
        format_spec=json.dumps(TRAINING_PLAN_FORMAT, ensure_ascii=False, indent=2)
    )

def build_fitness_qa_prompt(question: str, user_profile: Dict, retrieved_knowledge: List[Dict]) -> str:
    """构建健身咨询的 Prompt（含RAG检索结果）"""
    knowledge_text = "\n---\n".join([
        f"[参考{k['id']}] {k['content']}" for k in retrieved_knowledge
    ]) if retrieved_knowledge else "未找到直接相关的专业知识，请基于通用健身知识回答，并建议用户咨询专业教练。"
    
    return FITNESS_QA_TEMPLATE.format(
        system_role=SYSTEM_ROLE,
        safety_constraints=SAFETY_CONSTRAINTS,
        user_profile=json.dumps(user_profile, ensure_ascii=False, indent=2),
        retrieved_knowledge=knowledge_text,
        question=question
    )

def build_feedback_adjust_prompt(user_profile: Dict, current_plan: Dict, feedback: Dict, feedback_history: List[Dict]) -> str:
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
        format_spec=json.dumps(PLAN_ADJUST_FORMAT, ensure_ascii=False, indent=2)
    )
```

---

### Phase 3: GraphRAG 知识图谱检索增强

**方案说明:** 采用基于 NetworkX + sentence-transformers 的 GraphRAG 方案替代传统向量检索。GraphRAG 的核心优势在于：
- **关系建模**：将健身知识构建为语义知识图谱，节点间有类型化关系（前提动作、禁忌动作、替代动作、目标肌群等）
- **多跳推理**：用户问"卧推后肩膀疼"，不仅能检索"卧推"和"肩痛"的知识，还能沿图谱边遍历找到"肩袖损伤预防"→"肩部热身动作"→"卧推替代动作"
- **结构化上下文**：检索结果是子图而非零散片段，LLM 能理解知识之间的关联，生成更连贯、更安全的回答

#### Task 3.1: 整理 500+ 条健身知识（带关系标注）
**文件:** `GymApp/ai-service/data/fitness_knowledge.json`

- [ ] 创建分类知识体系（动作技术/训练计划/运动营养/运动生理/伤病康复/安全准则）
- [ ] 编写 500+ 条结构化知识片段
- [ ] 每条标注关系：prerequisite_for / contraindicates / alternative_to / targets / progression_of / safety_for

知识分类和数量分布：
| 分类 | 条目数 | 说明 |
|------|--------|------|
| 动作技术 | 150 | 各肌群标准动作要领、常见错误、修正方法、关系标注 |
| 训练计划设计 | 100 | 增肌/减脂/塑形/力量周期的计划设计原则 |
| 运动营养学 | 80 | 宏量营养素、补剂、饮食时机 |
| 运动生理学 | 70 | 肌肉生长机制、能量系统、激素调节 |
| 伤病预防与康复 | 60 | 常见训练伤预防、康复训练、动作修正 |
| 安全准则 | 40 | 训练安全红线、禁忌动作、危险信号识别 |

```json
[
  {
    "id": "EX-001",
    "category": "动作技术",
    "subcategory": "胸部训练",
    "tags": ["卧推", "杠铃", "胸部", "上肢推", "复合动作"],
    "content": "杠铃卧推时，肩胛骨应始终收紧下压，双脚稳定踩地，腰部保持自然生理弧度，杠铃下放至乳头连线水平，推起时肘关节不要完全锁定。常见错误：肩胛骨放松导致肩关节前移，增加肩袖损伤风险。",
    "difficulty": "intermediate",
    "target_muscle": "胸大肌、三角肌前束、肱三头肌",
    "source": "NSCA Essentials of Strength Training and Conditioning",
    "relationships": {
      "prerequisite": ["EX-101"],
      "contraindication_for": ["INJURY-003"],
      "alternative_to": ["EX-002"],
      "targets": ["MUSCLE-001", "MUSCLE-003", "MUSCLE-020"],
      "progression_of": ["EX-100"]
    }
  }
]
```

#### Task 3.2: 实现知识图谱构建器
**文件:** `GymApp/ai-service/rag/graph_builder.py`

- [ ] 实现 FitnessKnowledgeGraph 类（基于 NetworkX）
- [ ] 从 JSON 加载知识节点并构建类型化边
- [ ] 每个节点用 sentence-transformers 计算 embedding
- [ ] 实现图谱持久化（pickle + JSON）

```python
# rag/graph_builder.py
import json
import pickle
import networkx as nx
import numpy as np
from typing import List, Dict, Optional, Set, Tuple
from dataclasses import dataclass, field
from sentence_transformers import SentenceTransformer
from config import EMBEDDING_MODEL, GRAPH_RAG_PERSIST_DIR, GRAPH_SIMILARITY_THRESHOLD
import os


@dataclass
class KnowledgeNode:
    """知识图谱节点"""
    node_id: str
    category: str
    subcategory: str = ""
    content: str = ""
    tags: List[str] = field(default_factory=list)
    difficulty: str = ""
    target_muscle: str = ""
    source: str = ""
    embedding: Optional[np.ndarray] = None


class FitnessKnowledgeGraph:
    """健身知识图谱 — 基于 NetworkX 的 GraphRAG"""

    # 关系类型定义
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
        self.embedder = SentenceTransformer(EMBEDDING_MODEL)
        self._node_embeddings: Dict[str, np.ndarray] = {}
        os.makedirs(persist_dir, exist_ok=True)

    def load_from_json(self, filepath: str) -> int:
        """从 JSON 文件加载知识并构建图谱"""
        with open(filepath, "r", encoding="utf-8") as f:
            knowledge_list = json.load(f)

        for item in knowledge_list:
            node_id = item["id"]
            # 创建节点
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

            # 创建类型化边
            relationships = item.get("relationships", {})
            for rel_type, target_ids in relationships.items():
                if rel_type in self.RELATION_TYPES:
                    for target_id in target_ids:
                        # 确保目标节点存在（可能尚未加载，先占位）
                        if not self.graph.has_node(target_id):
                            self.graph.add_node(target_id, content="", category="")
                        self.graph.add_edge(
                            node_id, target_id,
                            relation=rel_type,
                            weight=self._get_edge_weight(rel_type)
                        )

        # 批量计算 embedding
        self._compute_embeddings()
        return len(knowledge_list)

    def _get_edge_weight(self, rel_type: str) -> float:
        """不同类型边的权重"""
        weights = {
            "prerequisite": 0.9,
            "progression_of": 0.8,
            "alternative_to": 0.7,
            "contraindication_for": 1.0,
            "targets": 0.4,
            "similar_to": 0.6,
            "safety_for": 0.95,
            "complements": 0.5,
        }
        return weights.get(rel_type, 0.5)

    def _compute_embeddings(self):
        """为所有有效节点计算文本 embedding"""
        texts = []
        node_ids = []
        for node_id, data in self.graph.nodes(data=True):
            text = self._node_to_text(node_id, data)
            if text.strip():
                texts.append(text)
                node_ids.append(node_id)

        if texts:
            embeddings = self.embedder.encode(texts, show_progress_bar=True)
            for i, node_id in enumerate(node_ids):
                self._node_embeddings[node_id] = embeddings[i].astype(np.float32)

    def _node_to_text(self, node_id: str, data: Dict) -> str:
        """将节点转为可 embedding 的文本"""
        parts = [
            f"ID: {node_id}",
            f"分类: {data.get('category', '')}",
            f"子分类: {data.get('subcategory', '')}",
            f"标签: {', '.join(data.get('tags', []))}",
            f"目标肌群: {data.get('target_muscle', '')}",
            f"难度: {data.get('difficulty', '')}",
            f"内容: {data.get('content', '')}",
        ]
        return "\n".join(parts)

    def save(self):
        """持久化图谱到磁盘"""
        # 保存 NetworkX 图
        graph_path = os.path.join(self.persist_dir, "knowledge_graph.gpickle")
        with open(graph_path, "wb") as f:
            pickle.dump(self.graph, f)

        # 保存 embeddings
        emb_path = os.path.join(self.persist_dir, "node_embeddings.npz")
        np.savez(emb_path, **self._node_embeddings)

    def load(self) -> bool:
        """从磁盘加载图谱"""
        graph_path = os.path.join(self.persist_dir, "knowledge_graph.gpickle")
        emb_path = os.path.join(self.persist_dir, "node_embeddings.npz")

        if not os.path.exists(graph_path) or not os.path.exists(emb_path):
            return False

        with open(graph_path, "rb") as f:
            self.graph = pickle.load(f)

        loaded = np.load(emb_path, allow_pickle=True)
        self._node_embeddings = {k: loaded[k] for k in loaded.files}
        return True

    def get_stats(self) -> Dict:
        """图谱统计"""
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
```

#### Task 3.3: 实现 GraphRAG 检索器（多跳子图检索）
**文件:** `GymApp/ai-service/rag/graph_retriever.py`

- [ ] 实现语义相似度初始节点匹配
- [ ] 实现多跳图遍历扩展（BFS + 类型过滤）
- [ ] 实现子图提取和上下文序列化
- [ ] 实现安全知识专项检索

```python
# rag/graph_retriever.py
import numpy as np
from typing import List, Dict, Optional, Set, Tuple
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
        # Step 1: 语义匹配种子节点
        seed_nodes = self._semantic_match(query, top_k=k)

        # Step 2: 多跳图遍历构建子图
        subgraph_nodes, subgraph_edges = self._multi_hop_expand(
            seed_nodes,
            max_hops=self.max_hops,
            category_filter=category_filter,
            difficulty=difficulty,
        )

        # Step 3: 提取结构化上下文
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
        """语义相似度匹配初始节点"""
        query_embedding = self.kg.embedder.encode([query])[0]

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

        # 按分数优先的 BFS
        queue = deque()
        for seed in seed_nodes:
            sid = seed["node_id"]
            visited.add(sid)
            queue.append((sid, 0, seed["score"]))

        while queue:
            node_id, hop, score = queue.popleft()
            if hop >= max_hops:
                continue

            # 按边的权重衰减 score
            for _, neighbor, edge_data in self.kg.graph.out_edges(node_id, data=True):
                edge_weight = edge_data.get("weight", 0.5)
                decayed_score = score * edge_weight

                # 过滤
                if not self._pass_filter(neighbor, category_filter, difficulty):
                    continue

                edges.append((node_id, neighbor, dict(edge_data), round(decayed_score, 4)))

                if neighbor not in visited and decayed_score >= self.similarity_threshold * 0.5:
                    visited.add(neighbor)
                    queue.append((neighbor, hop + 1, decayed_score))

            # 也检查入边（反向关系）
            for predecessor, _, edge_data in self.kg.graph.in_edges(node_id, data=True):
                edge_weight = edge_data.get("weight", 0.5)
                decayed_score = score * edge_weight * 0.8  # 反向关系略降权

                if not self._pass_filter(predecessor, category_filter, difficulty):
                    continue

                edges.append((predecessor, node_id, dict(edge_data), round(decayed_score, 4)))

                if predecessor not in visited and decayed_score >= self.similarity_threshold * 0.5:
                    visited.add(predecessor)
                    queue.append((predecessor, hop + 1, decayed_score))

        return visited, edges

    def _pass_filter(self, node_id: str, category_filter: Optional[str],
                     difficulty: Optional[str]) -> bool:
        """检查节点是否通过过滤条件"""
        node_data = self.kg.graph.nodes.get(node_id, {})
        if category_filter and node_data.get("category") != category_filter:
            return False
        if difficulty and node_data.get("difficulty") != difficulty:
            return False
        return True

    def _extract_nodes_data(self, node_ids: Set[str]) -> List[Dict]:
        """提取节点详情"""
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
        """找到种子节点之间的关键路径（最多2跳）"""
        seed_ids = {s["node_id"] for s in seed_nodes}
        paths = []

        for sid in seed_ids:
            for tid in seed_ids:
                if sid >= tid:
                    continue
                try:
                    path = nx.shortest_path(self.kg.graph, sid, tid)
                    if len(path) <= 4:  # 不超过4跳
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

        # 只保留最短的几条路径
        paths.sort(key=lambda p: p["length"])
        return paths[:10]

    def _serialize_context(self, nodes_data: List[Dict], paths: List[Dict]) -> str:
        """序列化子图为 LLM 可读的上下文文本"""

        # 按分类分组
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

        # 添加关键路径（知识关联推理链）
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
        """为训练计划生成定制的图谱检索"""
        query = f"{goal}训练计划 {fitness_level}水平"
        if injuries:
            query += f" 注意{injuries}"

        result = self.retrieve(query, k=8)

        # 额外检索安全相关子图
        safety_result = self.retrieve(
            f"{goal} {injuries or ''} 安全注意事项 动作禁忌",
            k=3, category_filter="安全准则"
        )

        result["safety_nodes"] = safety_result.get("subgraph_nodes", [])
        return result

    def retrieve_for_qa(self, question: str, user_level: str = "beginner") -> Dict:
        """为健身咨询定制的图谱检索"""
        return self.retrieve(
            question,
            k=5,
            difficulty=user_level if user_level != "advanced" else None
        )
```

#### Task 3.4: 实现知识加载器
**文件:** `GymApp/ai-service/rag/knowledge_loader.py`

- [ ] 实现知识片段加载和验证
- [ ] 实现图谱构建一站式接口
- [ ] 实现知识库统计和健康检查
- [ ] 验证 500+ 条知识加载 + 图谱构建成功

```python
# rag/knowledge_loader.py
import json
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
        # 先尝试从持久化存储加载
        if self.kg.load():
            stats = self.kg.get_stats()
            self.retriever = GraphRAGRetriever(self.kg)
            return {
                "source": "persisted",
                "stats": stats,
                "message": "从磁盘加载已有知识图谱"
            }

        # 从 JSON 构建新图谱
        count = self.kg.load_from_json(knowledge_file)
        self.kg.save()
        self.retriever = GraphRAGRetriever(self.kg)

        return {
            "source": "json",
            "total_loaded": count,
            "stats": self.kg.get_stats(),
            "message": f"成功从 JSON 加载 {count} 条知识并构建知识图谱"
        }

    def get_retriever(self) -> GraphRAGRetriever:
        if self.retriever is None:
            raise RuntimeError("知识图谱未初始化，请先调用 initialize()")
        return self.retriever

    def get_stats(self) -> Dict:
        return self.kg.get_stats()
```
    
    def load_from_file(self, filepath: str) -> int:
        """加载知识文件到向量库，返回加载条数"""
        return self.vs.load_knowledge(filepath)
    
    def get_stats(self) -> Dict:
        """获取知识库统计信息"""
        count = self.vs.collection.count()
        return {
            "total_entries": count,
            "collection_name": self.vs.collection_name
        }
```

---

### Phase 4: 多轮对话 Agent

#### Task 4.1: 实现对话状态管理器
**文件:** `GymApp/ai-service/agent/conversation_manager.py`

- [ ] 实现对话会话管理（创建、获取、过期清理）
- [ ] 实现对话上下文维护
- [ ] 实现对话历史摘要（超出 token 限制时压缩）
- [ ] 单元测试

```python
# agent/conversation_manager.py
from typing import Dict, List, Optional
from datetime import datetime, timedelta
from dataclasses import dataclass, field
import uuid
import json

@dataclass
class ConversationTurn:
    role: str  # "user" or "assistant"
    content: str
    timestamp: str = field(default_factory=lambda: datetime.now().isoformat())
    metadata: Dict = field(default_factory=dict)

@dataclass
class ConversationSession:
    session_id: str
    user_id: int
    context_type: str  # "training_consult", "plan_generation", "feedback_loop"
    turns: List[ConversationTurn] = field(default_factory=list)
    user_profile: Dict = field(default_factory=dict)
    created_at: str = field(default_factory=lambda: datetime.now().isoformat())
    updated_at: str = field(default_factory=lambda: datetime.now().isoformat())
    max_turns: int = 20
    
    def add_turn(self, role: str, content: str, metadata: Dict = None):
        self.turns.append(ConversationTurn(role=role, content=content, metadata=metadata or {}))
        self.updated_at = datetime.now().isoformat()
        # 保留最近 N 轮，防止上下文过长
        if len(self.turns) > self.max_turns * 2:
            # 保留前5轮（初始上下文）+最近15轮
            self.turns = self.turns[:5] + self.turns[-15:]
    
    def get_context_for_llm(self) -> List[Dict]:
        """获取适合发送给 LLM 的对话上下文"""
        return [{"role": t.role, "content": t.content} for t in self.turns]
    
    def summarize_history(self) -> str:
        """生成对话历史摘要"""
        if len(self.turns) <= 4:
            return "对话刚开始"
        recent_turns = self.turns[-8:]
        summary_parts = []
        for t in recent_turns:
            if t.role == "user":
                summary_parts.append(f"用户: {t.content[:100]}...")
        return "\n".join(summary_parts)


class ConversationManager:
    def __init__(self):
        self._sessions: Dict[str, ConversationSession] = {}
        self._session_ttl_hours = 24  # 会话过期时间
    
    def create_session(self, user_id: int, context_type: str, user_profile: Dict) -> ConversationSession:
        """创建新对话会话"""
        session_id = str(uuid.uuid4())
        session = ConversationSession(
            session_id=session_id,
            user_id=user_id,
            context_type=context_type,
            user_profile=user_profile
        )
        self._sessions[session_id] = session
        self._cleanup_expired()
        return session
    
    def get_session(self, session_id: str) -> Optional[ConversationSession]:
        """获取会话"""
        session = self._sessions.get(session_id)
        if session:
            # 检查是否过期
            if datetime.fromisoformat(session.updated_at) < datetime.now() - timedelta(hours=self._session_ttl_hours):
                del self._sessions[session_id]
                return None
            return session
        return None
    
    def add_message(self, session_id: str, role: str, content: str, metadata: Dict = None):
        """向会话添加消息"""
        session = self.get_session(session_id)
        if session:
            session.add_turn(role, content, metadata)
            return True
        return False
    
    def end_session(self, session_id: str):
        """结束会话"""
        if session_id in self._sessions:
            del self._sessions[session_id]
    
    def _cleanup_expired(self):
        """清理过期会话"""
        cutoff = datetime.now() - timedelta(hours=self._session_ttl_hours)
        expired = [
            sid for sid, s in self._sessions.items()
            if datetime.fromisoformat(s.updated_at) < cutoff
        ]
        for sid in expired:
            del self._sessions[sid]
```

#### Task 4.2: 实现训练反馈闭环
**文件:** `GymApp/ai-service/agent/feedback_loop.py`

- [ ] 实现反馈解析 — 从用户自然语言提取关键信息（不适部位、强度感受、完成度）
- [ ] 实现反馈历史汇总
- [ ] 实现与计划调整的联动

```python
# agent/feedback_loop.py
from typing import Dict, List
from datetime import datetime
import json

class FeedbackLoop:
    def __init__(self):
        self.body_part_keywords = {
            "膝盖": "knee",
            "腰部": "lower_back",
            "肩膀": "shoulder",
            "手腕": "wrist",
            "肘部": "elbow",
            "脚踝": "ankle",
            "颈部": "neck",
            "髋部": "hip",
            "背部": "back",
        }
    
    def parse_user_feedback(self, feedback_text: str) -> Dict:
        """从用户自然语言反馈中提取结构化信息"""
        result = {
            "pain_areas": [],
            "intensity_feedback": "normal",
            "completion_status": "completed",
            "specific_issues": [],
            "positive_notes": [],
        }
        
        # 检测不适部位
        for cn_part, en_part in self.body_part_keywords.items():
            if cn_part in feedback_text:
                result["pain_areas"].append({
                    "body_part_cn": cn_part,
                    "body_part_en": en_part,
                    "severity": "reported"
                })
        
        # 检测强度感受
        intensity_patterns = {
            "too_easy": ["太轻", "太轻松", "没感觉", "不够", "太简单"],
            "too_hard": ["太累", "受不了", "太猛", "太重", "做不了", "完不成"],
            "just_right": ["刚好", "合适", "不错", "正好", "舒服"],
        }
        for level, keywords in intensity_patterns.items():
            if any(kw in feedback_text for kw in keywords):
                result["intensity_feedback"] = level
        
        # 检测完成状态
        if any(kw in feedback_text for kw in ["没完成", "没做完", "只做了", "没练", "缺席"]):
            result["completion_status"] = "partial"
        
        return result
    
    def summarize_history(self, feedback_history: List[Dict]) -> Dict:
        """汇总历史反馈，识别趋势"""
        if not feedback_history:
            return {"trend": "无历史反馈数据", "recurring_issues": [], "progress": "未知"}
        
        # 统计不适部位出现频率
        pain_counts = {}
        for fb in feedback_history:
            for area in fb.get("pain_areas", []):
                key = area["body_part_cn"]
                pain_counts[key] = pain_counts.get(key, 0) + 1
        
        # 反复出现的问题（≥2次）
        recurring = [part for part, count in pain_counts.items() if count >= 2]
        
        return {
            "total_feedbacks": len(feedback_history),
            "recurring_issues": recurring,
            "pain_frequency": pain_counts,
            "trend": "improving" if not recurring else "needs_attention"
        }
```

#### Task 4.3: 实现计划自动调整器
**文件:** `GymApp/ai-service/agent/plan_adjuster.py`

- [ ] 实现基于规则的计划调整（动作替换、强度调整）
- [ ] 实现基于 AI 的智能调整
- [ ] 调整结果验证

```python
# agent/plan_adjuster.py
from typing import Dict, List
from .feedback_loop import FeedbackLoop
from prompts.safety_constraints import INJURY_ADJUSTMENTS

class PlanAdjuster:
    def __init__(self, feedback_loop: FeedbackLoop):
        self.feedback_loop = feedback_loop
    
    def rule_based_adjust(self, current_plan: Dict, parsed_feedback: Dict) -> Dict:
        """基于安全规则的自动调整（快速，无需调用AI）"""
        adjustments = []
        
        # 1. 处理伤痛部位的动作替换
        for pain_area in parsed_feedback.get("pain_areas", []):
            body_part = pain_area["body_part_cn"]
            if body_part in INJURY_ADJUSTMENTS:
                adj = INJURY_ADJUSTMENTS[body_part]
                adjustments.append({
                    "type": "injury_replacement",
                    "body_part": body_part,
                    "avoid_exercises": adj["avoid"],
                    "replace_with": adj["replace_with"],
                    "note": adj["note"]
                })
        
        # 2. 强度调整
        intensity = parsed_feedback.get("intensity_feedback", "normal")
        if intensity == "too_hard":
            adjustments.append({
                "type": "intensity_reduce",
                "action": "全局降低10%训练量",
                "detail": "减少1组或降低5-10%重量"
            })
        elif intensity == "too_easy":
            adjustments.append({
                "type": "intensity_increase",
                "action": "全局增加5-10%训练量",
                "detail": "可在安全范围内增加1组或微增重量"
            })
        
        return {
            "rule_based_adjustments": adjustments,
            "ai_refinement_needed": len(adjustments) > 0
        }
```

---

### Phase 5: API 路由实现

#### Task 5.1: 训练计划生成 API
**文件:** `GymApp/ai-service/routers/plan_generation.py`

- [ ] `POST /api/ai/plan/generate` — 完整 Prompt Engineering + RAG 增强的计划生成
- [ ] 接收体测数据，返回结构化训练计划
- [ ] 集成 RAG 检索相关安全知识

```python
# routers/plan_generation.py
from fastapi import APIRouter, HTTPException
from models.schemas import PlanGenerateRequest, PlanGenerateResponse
from prompts import build_training_plan_prompt
from rag.retriever import FitnessRetriever
from config import DEEPSEEK_API_KEY, DEEPSEEK_API_URL, DEEPSEEK_MODEL
import httpx
import json

router = APIRouter()

@router.post("/plan/generate", response_model=PlanGenerateResponse)
async def generate_training_plan(request: PlanGenerateRequest):
    # 1. RAG 检索安全相关知识
    retriever = router.retriever
    safety_knowledge = retriever.retrieve_safety_knowledge(
        f"{request.goal}训练计划 注意事项"
    )
    
    # 2. 构建 Prompt
    prompt = build_training_plan_prompt({
        "gender": request.gender,
        "age": request.age,
        "height": request.height,
        "weight": request.weight,
        "body_fat": request.body_fat,
        "training_years": request.training_years,
        "fitness_level": request.fitness_level,
        "injuries": request.injuries,
        "equipment": request.equipment,
        "goal": request.goal,
        "duration": request.duration,
        "days_per_week": request.days_per_week,
        "session_minutes": request.session_minutes,
        "additional_requirements": request.additional_requirements or "无",
        "safety_references": "\n".join([k["content"] for k in safety_knowledge])
    })
    
    # 3. 调用 DeepSeek API
    async with httpx.AsyncClient(timeout=120) as client:
        response = await client.post(
            DEEPSEEK_API_URL,
            headers={
                "Authorization": f"Bearer {DEEPSEEK_API_KEY}",
                "Content-Type": "application/json"
            },
            json={
                "model": DEEPSEEK_MODEL,
                "messages": [
                    {"role": "system", "content": prompt},
                    {"role": "user", "content": "请根据以上要求生成训练计划"}
                ],
                "temperature": 0.5,
                "max_tokens": 4000,
                "response_format": {"type": "json_object"}
            }
        )
        result = response.json()
    
    # 4. 解析和验证输出
    try:
        plan_content = result["choices"][0]["message"]["content"]
        plan_json = json.loads(plan_content)
    except (KeyError, json.JSONDecodeError) as e:
        raise HTTPException(status_code=500, detail=f"AI响应解析失败: {str(e)}")
    
    return PlanGenerateResponse(
        plan=plan_json,
        safety_references_used=[k["id"] for k in safety_knowledge],
        prompt_tokens_used=result.get("usage", {}).get("prompt_tokens", 0)
    )
```

#### Task 5.2: 健身咨询 API（多轮对话）
**文件:** `GymApp/ai-service/routers/fitness_qa.py`

- [ ] `POST /api/ai/qa/ask` — 带会话管理的多轮健身咨询
- [ ] 集成 RAG 知识检索
- [ ] `POST /api/ai/qa/session/new` — 创建新会话
- [ ] `GET /api/ai/qa/session/{session_id}` — 获取会话历史

#### Task 5.3: 训练反馈 API
**文件:** `GymApp/ai-service/routers/feedback.py`

- [ ] `POST /api/ai/feedback/submit` — 提交训练反馈
- [ ] `POST /api/ai/feedback/adjust-plan` — 基于反馈调整计划
- [ ] 实现反馈解析、历史汇总、AI 调整的完整闭环

---

### Phase 6: Java 后端适配

#### Task 6.1: 改造 AIService.java 调用 Python 服务
**文件:** `GymApp/backend/src/main/java/com/gym/service/AIService.java`

- [ ] 新增方法调用 Python AI 微服务
- [ ] 保留原有 DeepSeek 直接调用作为 fallback
- [ ] 实现请求/响应的 JSON 序列化

#### Task 6.2: 更新 application.properties
**文件:** `GymApp/backend/src/main/resources/application.properties`

- [ ] 添加 AI 服务地址配置 `ai.service.url=http://localhost:8000`
- [ ] 添加连接超时等配置

---

### Phase 7: Flutter 前端升级

#### Task 7.1: 升级 AI 计划生成页面
**文件:** `GymApp/frontend/lib/screens/ai_plan_generate_screen.dart`

- [ ] 增加体测数据输入表单（体脂率、训练年限等）
- [ ] 调用 AI 微服务生成计划
- [ ] 展示结构化训练计划（周视图+日视图）

#### Task 7.2: 升级 AI 助手为多轮对话
**文件:** `GymApp/frontend/lib/screens/ai_assistant_screen.dart`

- [ ] 实现聊天界面（气泡式对话）
- [ ] 会话管理（创建/切换/删除会话）
- [ ] 显示参考知识来源
- [ ] 发送训练反馈入口

#### Task 7.3: 增加训练反馈入口
**文件:** `GymApp/frontend/lib/screens/workout_checkin_screen.dart`

- [ ] 打卡后弹出反馈表单
- [ ] 预设反馈选项 + 自由文本输入
- [ ] 反馈提交后展示 AI 调整建议

#### Task 7.4: 新建 AI API 调用服务
**文件:** `GymApp/frontend/lib/services/ai_service.dart`

- [ ] 统一管理所有 AI API 调用
- [ ] 错误处理和重试
- [ ] 响应缓存

---

## 执行策略

推荐使用 **Subagent-Driven Development** 方式执行：
- 每个 Phase 由一个 subagent 负责
- Phase 1-3（Python 基础 + Prompt + RAG）可串行，Phase 4-5（Agent + API）紧跟其后
- Phase 6-7（Java + Flutter）在 Python 服务就绪后并行进行
