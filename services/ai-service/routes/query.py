from fastapi import APIRouter, Header, HTTPException
from fastapi.responses import StreamingResponse
from services.rag_service import rag_service
from models.schemas import QueryRequest, QueryResponse
import logging
import json

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/api/v1/ai", tags=["query"])

@router.post("/query", response_model=QueryResponse)
async def query_course(
        request: QueryRequest,
        x_tenant_id: str = Header(...),
        x_user_id: str = Header(...)
):
    """
    Ask a question about course content.

    Uses RAG to find relevant course materials and generate answer.

    Headers:
    - X-Tenant-Id: Tenant ID
    - X-User-Id: User ID (student)

    Body:
    - course_id: Course ID
    - question: Student question
    """
    try:
        response = await rag_service.query(request)
        return response

    except Exception as e:
        logger.error(f"Query error: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@router.post("/query/stream")
async def stream_query(
        request: QueryRequest,
        x_tenant_id: str = Header(...),
        x_user_id: str = Header(...)
):
    """
    Stream response token by token (Server-Sent Events).

    For real-time responses in frontend or live classes.
    """
    try:
        async def event_generator():
            async for token in rag_service.stream_query(request):
                yield f"data: {json.dumps({'token': token})}\n\n"

        return StreamingResponse(
            event_generator(),
            media_type="text/event-stream"
        )

    except Exception as e:
        logger.error(f"Stream query error: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))