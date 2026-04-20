import litellm
from typing import AsyncGenerator
import logging
from config import settings

logger = logging.getLogger(__name__)

class LLMProvider:
    """
    LLM Provider using LiteLLM abstraction.
    Supports: OpenAI, Anthropic, Groq, Local (Ollama), etc.
    Change MODEL_NAME in .env to switch providers without code changes.
    """

    def __init__(self):
        self.model_name = settings.MODEL_NAME
        self.temperature = settings.LLM_TEMPERATURE
        self.max_tokens = settings.LLM_MAX_TOKENS

        # Set API keys if needed
        if "openai" in self.model_name.lower():
            litellm.openai_key = settings.OPENAI_API_KEY
        elif "groq" in self.model_name.lower():
            litellm.groq_key = settings.GROQ_API_KEY
        elif "anthropic" in self.model_name.lower():
            litellm.anthropic_key = settings.ANTHROPIC_API_KEY

    async def generate_response(self, prompt: str, context: str = "") -> str:
        """
        Generate response from LLM.
        Returns complete response as string.
        """
        full_prompt = self._build_prompt(prompt, context)

        try:
            response = litellm.completion(
                model=self.model_name,
                messages=[{"role": "user", "content": full_prompt}],
                temperature=self.temperature,
                max_tokens=self.max_tokens,
                timeout=30
            )

            return response.choices[0].message.content

        except Exception as e:
            logger.error(f"LLM error: {str(e)}")
            raise

    async def stream_response(self, prompt: str, context: str = "") -> AsyncGenerator[str, None]:
        """
        Stream response from LLM token by token.
        Used for real-time responses in WebSocket.
        """
        full_prompt = self._build_prompt(prompt, context)

        try:
            response = litellm.completion(
                model=self.model_name,
                messages=[{"role": "user", "content": full_prompt}],
                temperature=self.temperature,
                max_tokens=self.max_tokens,
                stream=True,
                timeout=30
            )

            for chunk in response:
                if chunk.choices[0].delta.content:
                    yield chunk.choices[0].delta.content

        except Exception as e:
            logger.error(f"LLM streaming error: {str(e)}")
            raise

    def _build_prompt(self, user_query: str, context: str) -> str:
        """Build prompt with context from RAG."""
        system_prompt = """You are a helpful AI assistant for an educational platform.
        Answer questions based ONLY on the provided course content.
        If the information is not in the context, say: "I don't have information about that in the course materials."
        Be concise and educational."""

        if context:
            prompt = f"""{system_prompt}

Context from course materials:
{context}

Student Question: {user_query}

Answer:"""
        else:
            prompt = f"""{system_prompt}

Student Question: {user_query}

Answer:"""

        return prompt

# Singleton instance
llm_provider = LLMProvider()