from pydantic_settings import BaseSettings
from typing import Optional

class Settings(BaseSettings):
    # Service
    SERVICE_NAME: str = "ai-service"
    SERVICE_PORT: int = 8000
    SERVICE_HOST: str = "0.0.0.0"
    ENVIRONMENT: str = "development"

    # Consul Service Discovery
    CONSUL_HOST: str = "localhost"
    CONSUL_PORT: int = 8500
    SERVICE_ID: str = "ai-service-1"

    # MongoDB
    MONGO_URL: str = "mongodb://admin:admin123@localhost:27017/eduplatform?authSource=admin"
    MONGO_DB_NAME: str = "eduplatform"

    # LLM Configuration
    MODEL_NAME: str = "ollama/llama2"  # Can be: groq/llama2, gpt-4, claude-3-sonnet, etc.
    LLM_TEMPERATURE: float = 0.7
    LLM_MAX_TOKENS: int = 1000

    # Embeddings
    EMBED_MODEL: str = "sentence-transformers/all-MiniLM-L6-v2"
    EMBED_DIMENSION: int = 384

    # ChromaDB
    CHROMA_HOST: str = "localhost"
    CHROMA_PORT: int = 8003
    CHROMA_COLLECTION_NAME: str = "course_content"

    # API Keys (for cloud models)
    GROQ_API_KEY: Optional[str] = None
    OPENAI_API_KEY: Optional[str] = None
    ANTHROPIC_API_KEY: Optional[str] = None

    # RAG Configuration
    TOP_K_RESULTS: int = 5
    SIMILARITY_THRESHOLD: float = 0.7
    CHUNK_SIZE: int = 500
    CHUNK_OVERLAP: int = 50

    # Logging
    LOG_LEVEL: str = "INFO"

    class Config:
        env_file = ".env"
        case_sensitive = True

settings = Settings()