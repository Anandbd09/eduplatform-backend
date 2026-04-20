import chromadb
from config import settings
import logging

logger = logging.getLogger(__name__)

class ChromaService:
    """
    ChromaDB vector database service.
    Handles storing and retrieving embeddings.
    """

    def __init__(self):
        self.client = None
        self.collection = None

    def _ensure_collection(self):
        if self.collection is None:
            try:
                self.client = chromadb.HttpClient(
                    host=settings.CHROMA_HOST,
                    port=settings.CHROMA_PORT
                )
                logger.info(f"Connected to ChromaDB at {settings.CHROMA_HOST}:{settings.CHROMA_PORT}")

                self.collection = self.client.get_or_create_collection(
                    name=settings.CHROMA_COLLECTION_NAME,
                    metadata={"hnsw:space": "cosine"}
                )
            except Exception as e:
                logger.error(f"ChromaDB connection error: {str(e)}")
                raise
        return self.collection

    def add_documents(self, course_id: str, documents: list):
        """
        Add documents to ChromaDB.
        documents = [
            {
                "id": "doc_123",
                "text": "Document content",
                "embedding": [0.1, 0.2, ...],
                "metadata": {"course_id": "course_123", "type": "lesson"}
            }
        ]
        """
        try:
            ids = [doc["id"] for doc in documents]
            embeddings = [doc["embedding"] for doc in documents]
            documents_text = [doc["text"] for doc in documents]
            metadatas = [doc.get("metadata", {}) for doc in documents]

            self._ensure_collection().upsert(
                ids=ids,
                embeddings=embeddings,
                documents=documents_text,
                metadatas=metadatas
            )

            logger.info(f"Added {len(documents)} documents for course {course_id}")
            return {"added": len(documents), "course_id": course_id}

        except Exception as e:
            logger.error(f"Error adding documents: {str(e)}")
            raise

    def search(self, embedding: list, course_id: str, top_k: int = 5) -> list:
        """
        Search for similar documents.
        Returns top_k most similar documents with similarity scores.
        """
        try:
            results = self._ensure_collection().query(
                query_embeddings=[embedding],
                n_results=top_k,
                where={"course_id": course_id}  # Filter by course
            )

            documents = []
            for i in range(len(results["ids"][0])):
                documents.append({
                    "id": results["ids"][0][i],
                    "text": results["documents"][0][i],
                    "distance": results["distances"][0][i],  # Lower = more similar
                    "metadata": results["metadatas"][0][i]
                })

            return documents

        except Exception as e:
            logger.error(f"Error searching documents: {str(e)}")
            raise

    def delete_course_documents(self, course_id: str):
        """
        Delete all documents for a course.
        """
        try:
            self._ensure_collection().delete(
                where={"course_id": course_id}
            )
            logger.info(f"Deleted all documents for course {course_id}")
        except Exception as e:
            logger.error(f"Error deleting documents: {str(e)}")
            raise

chroma_service = None


def get_chroma_service() -> ChromaService:
    global chroma_service
    if chroma_service is None:
        chroma_service = ChromaService()
    return chroma_service
