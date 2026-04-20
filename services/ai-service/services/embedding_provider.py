import numpy as np
from config import settings
import logging

logger = logging.getLogger(__name__)

class EmbeddingProvider:
    """
    Embedding provider using sentence-transformers.
    Local, free, no API keys needed.
    """

    def __init__(self):
        self.model_name = settings.EMBED_MODEL
        self.model = None
        self.dimension = settings.EMBED_DIMENSION

    def _ensure_model(self):
        if self.model is None:
            from sentence_transformers import SentenceTransformer

            logger.info(f"Loading embedding model: {self.model_name}")
            self.model = SentenceTransformer(self.model_name)
        return self.model

    def embed_text(self, text: str) -> list:
        """
        Convert text to embedding vector.
        Returns: list of floats (dimension = EMBED_DIMENSION)
        """
        try:
            embedding = self._ensure_model().encode(text, convert_to_numpy=True)
            return embedding.tolist()
        except Exception as e:
            logger.error(f"Embedding error: {str(e)}")
            raise

    def embed_batch(self, texts: list) -> list:
        """
        Convert multiple texts to embeddings (faster than one-by-one).
        """
        try:
            embeddings = self._ensure_model().encode(texts, convert_to_numpy=True)
            return embeddings.tolist()
        except Exception as e:
            logger.error(f"Batch embedding error: {str(e)}")
            raise

    def similarity(self, embedding1: list, embedding2: list) -> float:
        """
        Calculate cosine similarity between two embeddings.
        Returns: score between 0 and 1
        """
        arr1 = np.array(embedding1)
        arr2 = np.array(embedding2)

        # Cosine similarity
        similarity = np.dot(arr1, arr2) / (np.linalg.norm(arr1) * np.linalg.norm(arr2))
        return float(similarity)

embedding_provider = None


def get_embedding_provider() -> EmbeddingProvider:
    global embedding_provider
    if embedding_provider is None:
        embedding_provider = EmbeddingProvider()
    return embedding_provider
