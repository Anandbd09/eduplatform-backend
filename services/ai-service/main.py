from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
import logging
import socket

try:
    import consul
except ImportError:
    consul = None

from config import settings
from routes import health, ingest, query


# Logging setup
logging.basicConfig(level=settings.LOG_LEVEL)
logger = logging.getLogger(__name__)


# Create FastAPI app
app = FastAPI(
    title="EduPlatform AI Service",
    description="AI-powered learning assistant with RAG pipeline",
    version="1.0.0",
)


# CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# Include routes
app.include_router(health.router)
app.include_router(ingest.router)
app.include_router(query.router)


@app.get("/actuator/health")
async def actuator_health():
    """Consul-compatible health endpoint."""
    return {"status": "UP"}


# Request middleware for tenant context
@app.middleware("http")
async def add_tenant_context(request, call_next):
    """Add tenant context from headers."""
    user_id = request.headers.get("X-User-Id")

    if not user_id and request.url.path not in ["/health", "/actuator/health"]:
        if request.method != "GET":
            return JSONResponse(status_code=401, content={"detail": "Unauthorized"})

    response = await call_next(request)
    return response


# Consul registration
@app.on_event("startup")
async def register_with_consul():
    """Register service with Consul."""
    if consul is None:
        logger.warning("Consul package not installed; skipping service registration")
        return

    try:
        c = consul.Consul(host=settings.CONSUL_HOST, port=settings.CONSUL_PORT)

        # Prefer loopback for local development so Consul can always reach the service.
        local_ip = "127.0.0.1"

        c.agent.service.register(
            name="ai-service",
            service_id=settings.SERVICE_ID,
            address=local_ip,
            port=settings.SERVICE_PORT,
            check=consul.Check.http(
                f"http://{local_ip}:{settings.SERVICE_PORT}/actuator/health",
                interval="10s",
                timeout="5s",
            ),
            tags=["service:ai", "version:1.0"],
        )

        logger.info("Registered with Consul: ai-service")

    except Exception as e:
        logger.error(f"Consul registration failed: {str(e)}")


@app.on_event("shutdown")
async def deregister_from_consul():
    """Deregister service from Consul."""
    if consul is None:
        return

    try:
        c = consul.Consul(host=settings.CONSUL_HOST, port=settings.CONSUL_PORT)
        c.agent.service.deregister(settings.SERVICE_ID)
        logger.info("Deregistered from Consul")

    except Exception as e:
        logger.error(f"Consul deregistration failed: {str(e)}")


@app.get("/")
async def root():
    """Root endpoint."""
    return {
        "service": settings.SERVICE_NAME,
        "version": "1.0.0",
        "status": "running",
        "model": settings.MODEL_NAME,
    }


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(
        "main:app",
        host=settings.SERVICE_HOST,
        port=settings.SERVICE_PORT,
        reload=settings.ENVIRONMENT == "development",
        log_level=settings.LOG_LEVEL.lower(),
    )
