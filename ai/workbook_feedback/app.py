from fastapi import FastAPI, HTTPException, Request, Header
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from typing import Optional, Any, List
from dotenv import load_dotenv
import logging
import json

# Load environment variables
load_dotenv()

from user_profiles import load_user_profile
from prompts import build_workbook_feedback_prompt, build_simulation_feedback_prompt
from models_openai import generate_feedback

app = FastAPI(title="I:ON Workbook Feedback API", version="1.0.0")

# logger
logger = logging.getLogger("uvicorn.error")


# ---------------- Error Standardization ---------------- #
class FeedbackAPIError(Exception):
    """Custom standardized error for feedback API."""
    def __init__(self, code: str, message: str, status_code: int = 400, details: Any = None):
        self.code = code
        self.message = message
        self.status_code = status_code
        self.details = details


@app.exception_handler(FeedbackAPIError)
async def feedback_api_error_handler(request: Request, exc: FeedbackAPIError):
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


@app.exception_handler(HTTPException)
async def http_exception_handler(request: Request, exc: HTTPException):
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


# ---------------- Request/Response Models ---------------- #
class DialogueItem(BaseModel):
    userLine: str
    aiLine: Optional[str] = None


class WorkbookFeedbackRequest(BaseModel):
    firstDescriptiveFormQuestion: str
    firstDescriptiveFormAnswer: str
    firstDescriptiveFormExample: str
    secondDescriptiveFormQuestion: str
    secondDescriptiveFormAnswer: str
    secondDescriptiveFormExample: str
    firstSelectiveQuestion: str
    firstSelectiveOptions: List[str]
    firstSelectiveAnswer: str
    firstSelectiveExample: str
    secondSelectiveQuestion: str
    secondSelectiveOptions: List[str]
    secondSelectiveAnswer: str
    secondSelectiveExample: str
    thirdSelectiveQuestion: str
    thirdSelectiveOptions: List[str]
    thirdSelectiveAnswer: str
    thirdSelectiveExample: str


class SimulationFeedbackRequest(BaseModel):
    dialogues: List[DialogueItem]


class WorkbookFeedbackResponse(BaseModel):
    workbookFeedback: str


class SimulationFeedbackResponse(BaseModel):
    simulationFeedback: str


# ---------------- Utility Functions ---------------- #
def _safe_json_parse(text: str) -> dict:
    """Parse strict JSON; tolerate ```json fences."""
    s = text.strip()
    if s.startswith("```"):
        s = s.strip("`")
        if "\n" in s:
            lines = s.split("\n")
            if lines[0].lower() in ["json", ""]:
                s = "\n".join(lines[1:])
    try:
        return json.loads(s)
    except Exception:
        start = s.find("{")
        end = s.rfind("}")
        if start != -1 and end != -1 and end > start:
            cand = s[start : end + 1]
            return json.loads(cand)
        raise


# ---------------- Health Check ---------------- #
@app.get("/")
def root():
    return {"message": "ION Workbook Feedback API is running", "version": "1.0.0"}


@app.get("/healthz")
def health_check():
    return {"message": "ION Workbook Feedback API is running", "version": "1.0.0", "status": "healthy"}


# ---------------- Workbook Feedback Endpoint ---------------- #
@app.post(
    "/api/workbook_feedback",
    response_model=WorkbookFeedbackResponse,
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
                            "message": "사용자 정보를 찾을 수 없습니다.",
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
    },
    summary="Generate feedback for workbook answers"
)
def create_workbook_feedback(
    payload: WorkbookFeedbackRequest,
    user_id: str = Header(..., alias="userId")
):
    try:
        # Log incoming request
        logger.info("[WORKBOOK_FEEDBACK] Received request - userId: %s", user_id)
        logger.info("[WORKBOOK_FEEDBACK] First Descriptive Q: %s", payload.firstDescriptiveFormQuestion[:50])
        logger.info("[WORKBOOK_FEEDBACK] Second Descriptive Q: %s", payload.secondDescriptiveFormQuestion[:50])
        
        # (1) Load user info from DB
        try:
            user_info = load_user_profile(user_id)
        except KeyError:
            raise FeedbackAPIError(
                code="USER_NOT_FOUND",
                message="해당 사용자 정보를 찾을 수 없습니다.",
                status_code=404,
                details=None
            )
        
        # (2) Build prompt
        prompt = build_workbook_feedback_prompt(
            user_info=user_info,
            first_descriptive_question=payload.firstDescriptiveFormQuestion,
            first_descriptive_answer=payload.firstDescriptiveFormAnswer,
            first_descriptive_example=payload.firstDescriptiveFormExample,
            second_descriptive_question=payload.secondDescriptiveFormQuestion,
            second_descriptive_answer=payload.secondDescriptiveFormAnswer,
            second_descriptive_example=payload.secondDescriptiveFormExample,
            first_selective_question=payload.firstSelectiveQuestion,
            first_selective_options=payload.firstSelectiveOptions,
            first_selective_answer=payload.firstSelectiveAnswer,
            first_selective_example=payload.firstSelectiveExample,
            second_selective_question=payload.secondSelectiveQuestion,
            second_selective_options=payload.secondSelectiveOptions,
            second_selective_answer=payload.secondSelectiveAnswer,
            second_selective_example=payload.secondSelectiveExample,
            third_selective_question=payload.thirdSelectiveQuestion,
            third_selective_options=payload.thirdSelectiveOptions,
            third_selective_answer=payload.thirdSelectiveAnswer,
            third_selective_example=payload.thirdSelectiveExample
        )
        
        # Log prompt for debugging
        logger.info("Generated Workbook Feedback Prompt for userId=%s", user_id)
        
        # (3) Call OpenAI API
        raw_response = generate_feedback(prompt)
        
        # (4) Parse JSON response
        try:
            feedback_data = _safe_json_parse(raw_response)
        except json.JSONDecodeError as e:
            logger.error("JSON parsing failed: %s", raw_response)
            raise FeedbackAPIError(
                code="PARSE_ERROR",
                message="피드백 생성 결과를 파싱할 수 없습니다.",
                status_code=500,
                details=str(e)
            )
        
        # (5) Return response
        return WorkbookFeedbackResponse(**feedback_data)
    
    except FeedbackAPIError:
        raise
    except Exception as e:
        logger.exception("Unexpected error in create_workbook_feedback")
        raise FeedbackAPIError(
            code="INTERNAL_ERROR",
            message="서버 내부 오류",
            status_code=500,
            details=str(e)
        )


# ---------------- Simulation Feedback Endpoint ---------------- #
@app.post(
    "/api/workbook_feedback_simulation",
    response_model=SimulationFeedbackResponse,
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
                            "message": "사용자 정보를 찾을 수 없습니다.",
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
    },
    summary="Generate feedback for simulation dialogues"
)
def create_simulation_feedback(
    payload: SimulationFeedbackRequest,
    user_id: str = Header(..., alias="userId")
):
    try:
        # Log incoming request
        logger.info("[SIMULATION_FEEDBACK] Received request - userId: %s, dialogues_count: %d", 
                   user_id, len(payload.dialogues))
        for idx, d in enumerate(payload.dialogues[:3], 1):  # Log first 3 dialogues
            logger.info("[SIMULATION_FEEDBACK] Dialogue %d - User: %s, AI: %s", 
                       idx, d.userLine[:30], d.aiLine[:30] if d.aiLine else "null")
        
        # (1) Load user info from DB
        try:
            user_info = load_user_profile(user_id)
        except KeyError:
            raise FeedbackAPIError(
                code="USER_NOT_FOUND",
                message="해당 사용자 정보를 찾을 수 없습니다.",
                status_code=404,
                details=None
            )
        
        # (2) Convert dialogues to dict list
        dialogues_list = [d.dict() for d in payload.dialogues]
        
        # (3) Build prompt
        prompt = build_simulation_feedback_prompt(user_info, dialogues_list)
        
        # Log prompt for debugging
        logger.info("Generated Simulation Feedback Prompt for userId=%s", user_id)
        
        # (4) Call OpenAI API
        raw_response = generate_feedback(prompt)
        
        # (5) Parse JSON response
        try:
            feedback_data = _safe_json_parse(raw_response)
        except json.JSONDecodeError as e:
            logger.error("JSON parsing failed: %s", raw_response)
            raise FeedbackAPIError(
                code="PARSE_ERROR",
                message="시뮬레이션 피드백 생성 결과를 파싱할 수 없습니다.",
                status_code=500,
                details=str(e)
            )
        
        # (6) Return response
        return SimulationFeedbackResponse(**feedback_data)
    
    except FeedbackAPIError:
        raise
    except Exception as e:
        logger.exception("Unexpected error in create_simulation_feedback")
        raise FeedbackAPIError(
            code="INTERNAL_ERROR",
            message="서버 내부 오류",
            status_code=500,
            details=str(e)
        )
