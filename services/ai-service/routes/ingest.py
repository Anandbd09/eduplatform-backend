from fastapi import APIRouter, UploadFile, File, Header, HTTPException, Response
from services.rag_service import rag_service
from models.schemas import IngestResponse
import logging

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/api/v1/ai", tags=["ingest"])

@router.post("/ingest", response_model=IngestResponse)
async def ingest_document(
        file: UploadFile = File(...),
        x_tenant_id: str = Header(...),
        x_user_id: str = Header(...),
        x_course_id: str = Header(...)
):
    """
    Ingest a course document (PDF or text).

    Headers:
    - X-Tenant-Id: Tenant ID
    - X-User-Id: User ID (instructor)
    - X-Course-Id: Course ID

    Body:
    - file: PDF or text file
    """
    try:
        # Read file
        contents = await file.read()

        if not contents:
            raise HTTPException(status_code=400, detail="File is empty")

        # Ingest using RAG service
        result = await rag_service.ingest_document(
            x_course_id,
            contents,
            file.filename
        )

        return IngestResponse(**result)

    except Exception as e:
        logger.error(f"Ingest error: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))