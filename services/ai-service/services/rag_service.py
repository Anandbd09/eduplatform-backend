from typing import List, Optional
from services.embedding_provider import get_embedding_provider
from services.chroma_service import get_chroma_service
from services.llm_provider import llm_provider
from models.schemas import QueryRequest, QueryResponse
import logging
from config import settings
import PyPDF2
import io

logger = logging.getLogger(__name__)

class RAGService:
    """
    Retrieval Augmented Generation (RAG) Service.
    Ingests documents, embeddings, stores in vector DB, and generates responses.
    """

    def __init__(self):
        self.chunk_size = settings.CHUNK_SIZE
        self.chunk_overlap = settings.CHUNK_OVERLAP
        self.top_k = settings.TOP_K_RESULTS

    async def ingest_document(self, course_id: str, file_bytes: bytes, filename: str) -> dict:
        """
        Ingest PDF/text document for a course.
        1. Extract text
        2. Split into chunks
        3. Generate embeddings
        4. Store in ChromaDB
        """
        try:
            # Extract text from file
            if filename.endswith(".pdf"):
                text = self._extract_pdf_text(file_bytes)
            else:
                text = file_bytes.decode("utf-8")

            # Split into chunks
            chunks = self._chunk_text(text, self.chunk_size, self.chunk_overlap)

            # Generate embeddings for all chunks
            chunk_texts = [chunk["text"] for chunk in chunks]
            embeddings = get_embedding_provider().embed_batch(chunk_texts)

            # Prepare documents for ChromaDB
            documents = []
            for i, chunk in enumerate(chunks):
                documents.append({
                    "id": f"{course_id}_chunk_{i}",
                    "text": chunk["text"],
                    "embedding": embeddings[i],
                    "metadata": {
                        "course_id": course_id,
                        "chunk_index": i,
                        "source": filename
                    }
                })

            # Store in ChromaDB
            get_chroma_service().add_documents(course_id, documents)

            logger.info(f"Ingested {len(documents)} chunks from {filename} for course {course_id}")

            return {
                "success": True,
                "course_id": course_id,
                "chunks_ingested": len(documents),
                "filename": filename
            }

        except Exception as e:
            logger.error(f"Error ingesting document: {str(e)}")
            raise

    async def query(self, query_request: QueryRequest) -> QueryResponse:
        """
        Answer a question using RAG.
        1. Embed the question
        2. Search for relevant documents
        3. Generate response using LLM with context
        """
        try:
            # Embed the question
            question_embedding = get_embedding_provider().embed_text(query_request.question)

            # Search for similar documents
            similar_docs = get_chroma_service().search(
                question_embedding,
                query_request.course_id,
                self.top_k
            )

            # Filter by similarity threshold
            relevant_docs = [
                doc for doc in similar_docs
                if (1 - doc["distance"]) >= settings.SIMILARITY_THRESHOLD
            ]

            # Build context from retrieved documents
            context = "\n\n".join([doc["text"] for doc in relevant_docs])

            # Generate response
            if context:
                response_text = await llm_provider.generate_response(
                    query_request.question,
                    context
                )
            else:
                response_text = "I don't have relevant information about this topic in the course materials."

            logger.info(f"Answered question for course {query_request.course_id}")

            return QueryResponse(
                course_id=query_request.course_id,
                question=query_request.question,
                answer=response_text,
                sources_count=len(relevant_docs),
                model_used=settings.MODEL_NAME
            )

        except Exception as e:
            logger.error(f"Error in RAG query: {str(e)}")
            raise

    async def stream_query(self, query_request: QueryRequest):
        """
        Stream response token by token.
        Used for real-time responses in WebSocket.
        """
        try:
            # Embed and search (same as above)
            question_embedding = get_embedding_provider().embed_text(query_request.question)
            similar_docs = get_chroma_service().search(
                question_embedding,
                query_request.course_id,
                self.top_k
            )

            relevant_docs = [
                doc for doc in similar_docs
                if (1 - doc["distance"]) >= settings.SIMILARITY_THRESHOLD
            ]

            context = "\n\n".join([doc["text"] for doc in relevant_docs])

            # Stream response
            async for token in llm_provider.stream_response(
                    query_request.question,
                    context
            ):
                yield token

        except Exception as e:
            logger.error(f"Error in stream query: {str(e)}")
            raise

    def _extract_pdf_text(self, file_bytes: bytes) -> str:
        """Extract text from PDF."""
        pdf_reader = PyPDF2.PdfReader(io.BytesIO(file_bytes))
        text = ""
        for page in pdf_reader.pages:
            text += page.extract_text() + "\n"
        return text

    def _chunk_text(self, text: str, chunk_size: int, overlap: int) -> list:
        """
        Split text into overlapping chunks.
        """
        chunks = []
        words = text.split()

        start = 0
        while start < len(words):
            end = min(start + chunk_size, len(words))
            chunk_text = " ".join(words[start:end])
            chunks.append({"text": chunk_text, "start": start, "end": end})
            start += chunk_size - overlap

        return chunks

# Singleton instance
rag_service = RAGService()
