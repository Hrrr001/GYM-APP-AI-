# Gym AI — 智能健身助手

面向健身人群的 AI 辅助 Android 应用，集成 **AI 训练计划生成**、**多轮健身咨询**、**训练反馈闭环** 三大核心功能。

## 技术栈

| 层 | 技术 |
|---|---|
| 前端 | Flutter (Android) |
| 后端 | Java Spring Boot 2.7 + Python FastAPI |
| AI | DeepSeek API |
| RAG | GraphRAG — NetworkX 知识图谱 + TF-IDF 本地嵌入 |
| 数据库 | MySQL 8 |
| 微服务通信 | REST (HTTP) |

## 系统架构

```
Flutter (Android)
       │
       ▼
Java Spring Boot (8080)  ──▶  MySQL
       │
       ▼
Python FastAPI (8000)  ──▶  DeepSeek API
       │
       ├── GraphRAG (NetworkX + TF-IDF)
       ├── Prompt Engineering (角色/安全/格式)
       └── 对话状态管理 (ConversationManager)
```

## 项目结构

```
GymApp/
├── ai-service/                 # Python AI 微服务
│   ├── main.py                 # FastAPI 入口
│   ├── config.py               # 配置 (API密钥/MySQL/GraphRAG)
│   ├── llm_client.py           # DeepSeek API 异步客户端
│   ├── prompts/                # Prompt Engineering
│   │   ├── templates.py        # 角色设定 + 安全约束 + Prompt 模板
│   │   ├── safety_constraints.py  # 高风险动作列表 + 伤病调整映射
│   │   └── format_specs.py     # JSON 输出格式规范
│   ├── rag/                    # GraphRAG 知识图谱
│   │   ├── graph_builder.py    # NetworkX 图构建 + TF-IDF 嵌入
│   │   ├── graph_retriever.py  # 语义匹配 + BFS 多跳遍历检索
│   │   └── knowledge_loader.py # 图谱初始化/持久化
│   ├── agent/                  # 多轮对话 Agent
│   │   ├── conversation_manager.py  # 会话状态管理
│   │   ├── feedback_loop.py    # 用户反馈解析
│   │   └── plan_adjuster.py    # 规则驱动计划调整
│   ├── routers/                # API 路由
│   │   ├── plan_generation.py  # 训练计划生成
│   │   ├── fitness_qa.py       # 多轮健身问答
│   │   └── feedback.py         # 训练反馈闭环
│   └── data/fitness_knowledge.json  # 525 条专业知识
│
├── backend/                    # Java Spring Boot 后端
│   └── src/main/java/com/gym/
│       ├── service/AIService.java    # AI 微服务代理
│       └── controller/AIController.java
│
└── frontend/                   # Flutter 前端 (15+ 页面)
    └── lib/screens/
        ├── ai_assistant_screen.dart        # 多轮对话助手
        ├── ai_plan_generate_screen.dart    # 计划生成
        └── training_feedback_screen.dart   # 训练反馈闭环
```

## 快速开始

### 1. 环境要求

- Python 3.10+
- Java 17+ & Maven
- Flutter 3.5+
- MySQL 8.0

### 2. Python AI 服务

```bash
cd GymApp/ai-service
pip install -r requirements.txt
cp .env.example .env          # 编辑填入你的 DeepSeek API Key
python main.py                # 启动于 0.0.0.0:8000
```

### 3. Java 后端

```bash
cd GymApp/backend
# 编辑 src/main/resources/application.properties
#   填入数据库密码和 DeepSeek API Key
mvn spring-boot:run            # 启动于 8080
```

### 4. Flutter 前端

```bash
cd GymApp/frontend
flutter pub get
flutter run
```

## GraphRAG 知识图谱

- **525 条**专业知识条目，来源：NSCA Essentials 4th Ed. / ACSM Guidelines 12th Ed. / ISSN Position Stands
- **8 种关系类型**：prerequisite / progression_of / alternative_to / contraindication_for / targets / similar_to / safety_for / complements
- **7 大类别**：动作技术(126) / 运动营养学(117) / 运动生理学(93) / 伤病预防与康复(58) / 安全准则(62) / 训练计划设计(54)

## API 端点

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/ai/plan/generate` | 生成个性化训练计划 |
| POST | `/api/ai/qa/ask` | 多轮健身问答 |
| POST | `/api/ai/qa/session/new` | 创建新对话会话 |
| GET | `/api/ai/qa/session/{id}` | 获取会话历史 |
| POST | `/api/ai/feedback/submit` | 提交训练反馈 |
| POST | `/api/ai/feedback/adjust-plan` | 基于反馈调整计划 |
