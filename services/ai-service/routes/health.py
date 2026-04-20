from fastapi import APIRouter, Response
from models.schemas import HealthResponse
from config import settings

router = APIRouter(prefix="/api/v1", tags=["health"])

@router.get("/health")
async def health_check():
    """Health check endpoint."""
    return HealthResponse(
        status="UP",
        service=settings.SERVICE_NAME,
        model=settings.MODEL_NAME
    )

@router.get("/actuator/health")
async def actuator_health():
    """Spring Boot style health endpoint for Consul."""
    return {"status": "UP"}