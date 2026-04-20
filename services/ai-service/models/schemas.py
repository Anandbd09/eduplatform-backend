from pydantic import BaseModel, Field, ConfigDict
from typing import Optional
from datetime import datetime

class IngestRequest(BaseModel):
    course_id: str = Field(..., description="Course ID")
    filename: str = Field(..., description="Document filename")

class IngestResponse(BaseModel):
    success: bool
    course_id: str
    chunks_ingested: int
    filename: str

class QueryRequest(BaseModel):
    course_id: str = Field(..., description="Course ID")
    question: str = Field(..., description="Student question")
    user_id: Optional[str] = None

class QueryResponse(BaseModel):
    model_config = ConfigDict(protected_namespaces=())
    course_id: str
    question: str
    answer: str
    sources_count: int
    model_used: str
    timestamp: datetime = Field(default_factory=datetime.now)

class HealthResponse(BaseModel):
    status: str
    service: str
    model: str
    timestamp: datetime = Field(default_factory=datetime.now)