import os
from dotenv import load_dotenv

load_dotenv()

DEEPSEEK_API_KEY = os.getenv("DEEPSEEK_API_KEY", "")
DEEPSEEK_API_URL = "https://api.deepseek.com/v1/chat/completions"
DEEPSEEK_MODEL = "deepseek-chat"

# GraphRAG 配置
GRAPH_RAG_PERSIST_DIR = os.getenv("GRAPH_RAG_PERSIST_DIR", "./graphrag_db")
EMBEDDING_MODEL = os.getenv("EMBEDDING_MODEL", "tfidf-local")
GRAPH_SIMILARITY_THRESHOLD = float(os.getenv("GRAPH_SIMILARITY_THRESHOLD", "0.5"))
GRAPH_MAX_HOPS = int(os.getenv("GRAPH_MAX_HOPS", "3"))

MYSQL_HOST = os.getenv("MYSQL_HOST", "localhost")
MYSQL_PORT = int(os.getenv("MYSQL_PORT", "3306"))
MYSQL_USER = os.getenv("MYSQL_USER", "root")
MYSQL_PASSWORD = os.getenv("MYSQL_PASSWORD", "")
MYSQL_DATABASE = os.getenv("MYSQL_DATABASE", "gym_app")

AI_SERVICE_PORT = int(os.getenv("AI_SERVICE_PORT", "8000"))
