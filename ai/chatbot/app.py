from fastapi import FastAPI, HTTPException, Request, Header

from fastapi.responses import JSONResponse
from pydantic import BaseModel
from typing import Optional, Any
from dotenv import load_dotenv
import logging

# Load environment variables from .env file
load_dotenv()

from user_profiles import load_user_profile
from prompts import build_chatbot_prompt, build_chatbot_prompt_with_context
from models_openai import generate_openai_answer
from rds_vector_search import retrieve_top_k

app = FastAPI(title="I:ON Chatbot API", version="1.0.0")

# logger that integrates with uvicorn/gunicorn
logger = logging.getLogger("uvicorn.error")

# ---------------- Error Standardization (v1 keeps success schema) ---------------- #
class ChatbotAPIError(Exception):
    """Custom standardized error for chatbot API.

    We keep success response as originally ({"answer": ...}).
    Errors now return:
    {
      "success": false,
      "error": {"code": str, "message": str, "details": optional}
    }
    """
    def __init__(self, code: str, message: str, status_code: int = 400, details: Any = None):
        self.code = code
        self.message = message
        self.status_code = status_code
        self.details = details


@app.exception_handler(ChatbotAPIError)
async def chatbot_api_error_handler(request: Request, exc: ChatbotAPIError):
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "success": False,
            "error": {
                "code": exc.code,
                "message": exc.message,
                "details": exc.details
            }
        }
    )

# Fallback: convert generic HTTPException into standardized format (except 422 validation)
@app.exception_handler(HTTPException)
async def http_exception_handler(request: Request, exc: HTTPException):
    # Let FastAPI handle validation errors (422) to avoid breaking tooling
    if exc.status_code == 422:
        return JSONResponse(status_code=exc.status_code, content={"detail": exc.detail})
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "success": False,
            "error": {
                "code": "HTTP_ERROR",
                "message": str(exc.detail),
                "details": None
            }
        }
    )

# Health check endpoint
@app.get("/")
def root():
    return {"message": "ION Chatbot API is running", "version": "1.0.0"}


class QuestionItem(BaseModel):
    question: str
    answer: Optional[str] = None

class ChatRequest(BaseModel):
    sessionId: int
    questions: list[QuestionItem]

class ChatResponse(BaseModel):
    answer: str

@app.post(
    "/api/chatbot",
    response_model=ChatResponse,
    responses={
        200: {"description": "OK"},
        404: {
            "description": "User not found",
            "content": {
                "application/json": {
                    "example": {
                        "success": False,
                        "error": {
                            "code": "USER_NOT_FOUND",
                            "message": "해당 사용자 정보를 찾을 수 없습니다.",
                            "details": None
                        }
                    }
                }
            }
        },
        500: {
            "description": "Internal error",
            "content": {
                "application/json": {
                    "example": {
                        "success": False,
                        "error": {
                            "code": "INTERNAL_ERROR",
                            "message": "서버 내부 오류",
                            "details": None
                        }
                    }
                }
            }
        }
    }
)

def chatbot_endpoint(payload: ChatRequest, user_id: str = Header(..., alias="userId")):
    try:
        # Extract the first question from the questions list
        if not payload.questions or len(payload.questions) == 0:
            raise ChatbotAPIError(
                code="INVALID_REQUEST",
                message="questions 배열이 비어있습니다.",
                status_code=400,
                details=None
            )
        
        # Build conversation history from all questions except the last one
        conversation_history = []
        for item in payload.questions[:-1]:
            conversation_history.append({"question": item.question, "answer": item.answer or ""})
        
        # Current question is the last item
        current_question = payload.questions[-1].question

        # (1) Load user info from rds
        user_info = load_user_profile(user_id)

        # (2) Retrieve relevant documents from vector DB (RDS)
        try:
            contexts = retrieve_top_k(current_question, k=2)
        except Exception as e:
            # If retrieval fails, log/continue with no context
            contexts = []

        if contexts:
            paragraphs = [c.get("paragraph", "") for c in contexts]
            prompt = build_chatbot_prompt_with_context(current_question, user_info, paragraphs, conversation_history)
            logger.info("=" * 80)
            logger.info("[CHATBOT PROMPT WITH CONTEXT]")
            logger.info("Question: %s", current_question)
            logger.info("Retrieved %d documents", len(contexts))
            logger.info("-" * 80)
            logger.info("PROMPT:\n%s", prompt)
            logger.info("=" * 80)
        else:
            # fallback: no retrieval
            prompt = build_chatbot_prompt(current_question, user_info, conversation_history)
            logger.info("=" * 80)
            logger.info("[CHATBOT PROMPT WITHOUT CONTEXT]")
            logger.info("Question: %s", current_question)
            logger.info("-" * 80)
            logger.info("PROMPT:\n%s", prompt)
            logger.info("=" * 80)

        # (3) Call OpenAI API, generate answer
        answer = generate_openai_answer(prompt)

        # return the model's answer (not the prompt)
        return ChatResponse(answer=answer)

    except KeyError:
        # User not found
        raise ChatbotAPIError(
            code="USER_NOT_FOUND",
            message="해당 사용자 정보를 찾을 수 없습니다.",
            status_code=404,
            details=None
        )
    except ChatbotAPIError:
        raise  # rethrow already standardized
    except Exception as e:
        # Unexpected error; hide raw details in message, keep details in dev
        raise ChatbotAPIError(
            code="INTERNAL_ERROR",
            message="서버 내부 오류",
            status_code=500,
            details=str(e)
        )
