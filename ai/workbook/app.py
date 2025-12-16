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
from workbook_theory import load_chapter_info
from prompts import build_workbook_prompt, build_simulation_prompt
from models_openai import generate_workbook

app = FastAPI(title="I:ON Workbook API", version="1.0.0")

# logger
logger = logging.getLogger("uvicorn.error")


# ---------------- Error Standardization ---------------- #
class WorkbookAPIError(Exception):
    """Custom standardized error for workbook API."""
    def __init__(self, code: str, message: str, status_code: int = 400, details: Any = None):
        self.code = code
        self.message = message
        self.status_code = status_code
        self.details = details


@app.exception_handler(WorkbookAPIError)
async def workbook_api_error_handler(request: Request, exc: WorkbookAPIError):
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


# ---------------- Response Models ---------------- #
class DialogueItem(BaseModel):
    userLine: str
    aiLine: Optional[str] = None


class WorkbookRequest(BaseModel):
    chapterId: int
    lessonId: int


class SimulationRequest(BaseModel):
    chapterId: int
    lessonId: int
    dialogues: Optional[List[DialogueItem]] = None


class WorkbookResponse(BaseModel):
    chapterTitle: str
    lessonTitle: str
    firstDescriptiveFormQuestion: str
    firstDescriptiveFormExample: str
    secondDescriptiveFormQuestion: str
    secondDescriptiveFormExample: str
    firstSelectiveQuestion: str
    firstSelectiveOptions: List[str]
    firstSelectiveExample: str
    secondSelectiveQuestion: str
    secondSelectiveOptions: List[str]
    secondSelectiveExample: str
    thirdSelectiveQuestion: str
    thirdSelectiveOptions: List[str]
    thirdSelectiveExample: str


class SimulationResponse(BaseModel):
    chapterTitle: str
    lessonTitle: str
    simulationSituationExplain: str
    aiLine: str


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
    return {"message": "ION Workbook API is running", "version": "1.0.0"}


@app.get("/healthz")
def health_check():
    return {"message": "ION Workbook API is running", "version": "1.0.0", "status": "healthy"}


# ---------------- Main Endpoint ---------------- #
@app.post(
    "/api/workbooks",
    response_model=WorkbookResponse,
    responses={
        200: {"description": "OK"},
        400: {
            "description": "Invalid request",
            "content": {
                "application/json": {
                    "example": {
                        "success": False,
                        "error": {
                            "code": "INVALID_REQUEST",
                            "message": "잘못된 요청입니다.",
                            "details": None
                        }
                    }
                }
            }
        },
        404: {
            "description": "User or Chapter not found",
            "content": {
                "application/json": {
                    "example": {
                        "success": False,
                        "error": {
                            "code": "NOT_FOUND",
                            "message": "사용자 또는 챕터 정보를 찾을 수 없습니다.",
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
    summary="Generate workbook based on chapter and lesson"
)
def create_workbook(
    payload: WorkbookRequest,
    user_id: str = Header(..., alias="userId")
):
    try:
        # Log incoming request
        logger.info("[WORKBOOK] Received request - userId: %s, chapterId: %d, lessonId: %d", 
                   user_id, payload.chapterId, payload.lessonId)
        
        # Validate lessonId
        if payload.lessonId not in [1, 2, 3, 4]:
            raise WorkbookAPIError(
                code="INVALID_REQUEST",
                message="lessonId는 1, 2, 3, 4 중 하나여야 합니다.",
                status_code=400,
                details={"lessonId": payload.lessonId}
            )
        
        # (1) Load user info from DB
        try:
            user_info = load_user_profile(user_id)
        except KeyError:
            raise WorkbookAPIError(
                code="USER_NOT_FOUND",
                message="해당 사용자 정보를 찾을 수 없습니다.",
                status_code=404,
                details=None
            )
        
        # (2) Load chapter info from DB
        try:
            chapter_info = load_chapter_info(payload.chapterId)
        except KeyError:
            raise WorkbookAPIError(
                code="CHAPTER_NOT_FOUND",
                message="해당 챕터 정보를 찾을 수 없습니다.",
                status_code=404,
                details={"chapterId": payload.chapterId}
            )
        
        # (3) Build prompt
        prompt = build_workbook_prompt(user_info, chapter_info, payload.lessonId)
        
        # Log prompt for debugging
        logger.info("Generated Prompt for chapterId=%d, lessonId=%d", payload.chapterId, payload.lessonId)
        
        # (4) Call OpenAI API
        raw_response = generate_workbook(prompt)
        
        # (5) Parse JSON response
        try:
            workbook_data = _safe_json_parse(raw_response)
        except json.JSONDecodeError as e:
            logger.error("JSON parsing failed: %s", raw_response)
            raise WorkbookAPIError(
                code="PARSE_ERROR",
                message="워크북 생성 결과를 파싱할 수 없습니다.",
                status_code=500,
                details=str(e)
            )
        
        # (6) Return response
        return WorkbookResponse(**workbook_data)
    
    except WorkbookAPIError:
        raise
    except Exception as e:
        logger.exception("Unexpected error in create_workbook")
        raise WorkbookAPIError(
            code="INTERNAL_ERROR",
            message="서버 내부 오류",
            status_code=500,
            details=str(e)
        )


# ---------------- Simulation Endpoint ---------------- #
@app.post(
    "/api/workbook_simulation",
    response_model=SimulationResponse,
    responses={
        200: {"description": "OK"},
        400: {
            "description": "Invalid request",
            "content": {
                "application/json": {
                    "example": {
                        "success": False,
                        "error": {
                            "code": "INVALID_REQUEST",
                            "message": "잘못된 요청입니다.",
                            "details": None
                        }
                    }
                }
            }
        },
        404: {
            "description": "User or Chapter not found",
            "content": {
                "application/json": {
                    "example": {
                        "success": False,
                        "error": {
                            "code": "NOT_FOUND",
                            "message": "사용자 또는 챕터 정보를 찾을 수 없습니다.",
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
    summary="Generate simulation workbook with interactive dialogue"
)
def create_simulation(
    payload: SimulationRequest,
    user_id: str = Header(..., alias="userId")
):
    try:
        # Log incoming request
        logger.info("[SIMULATION] Received request - userId: %s, chapterId: %d, lessonId: %d, dialogues: %s", 
                   user_id, payload.chapterId, payload.lessonId, 
                   "present" if payload.dialogues else "null")
        
        # Validate lessonId (simulation은 무조건 5)
        if payload.lessonId != 5:
            raise WorkbookAPIError(
                code="INVALID_REQUEST",
                message="시뮬레이션 lessonId는 5여야 합니다.",
                status_code=400,
                details={"lessonId": payload.lessonId}
            )
        
        # (1) Load user info from DB
        try:
            user_info = load_user_profile(user_id)
        except KeyError:
            raise WorkbookAPIError(
                code="USER_NOT_FOUND",
                message="해당 사용자 정보를 찾을 수 없습니다.",
                status_code=404,
                details=None
            )
        
        # (2) Load chapter info from DB
        try:
            chapter_info = load_chapter_info(payload.chapterId)
        except KeyError:
            raise WorkbookAPIError(
                code="CHAPTER_NOT_FOUND",
                message="해당 챕터 정보를 찾을 수 없습니다.",
                status_code=404,
                details={"chapterId": payload.chapterId}
            )
        
        # (3) Convert dialogues to dict list
        dialogues_list = None
        if payload.dialogues:
            dialogues_list = [d.dict() for d in payload.dialogues]
        
        # (4) Build prompt
        prompt = build_simulation_prompt(user_info, chapter_info, dialogues_list)
        
        # Log prompt for debugging
        logger.info("Generated Simulation Prompt for chapterId=%d, lessonId=%d, has_dialogues=%s", 
                   payload.chapterId, payload.lessonId, payload.dialogues is not None)
        
        # (5) Call OpenAI API
        raw_response = generate_workbook(prompt)
        
        # (6) Parse JSON response
        try:
            simulation_data = _safe_json_parse(raw_response)
        except json.JSONDecodeError as e:
            logger.error("JSON parsing failed: %s", raw_response)
            raise WorkbookAPIError(
                code="PARSE_ERROR",
                message="시뮬레이션 생성 결과를 파싱할 수 없습니다.",
                status_code=500,
                details=str(e)
            )
        
        # (7) Return response
        return SimulationResponse(**simulation_data)
    
    except WorkbookAPIError:
        raise
    except Exception as e:
        logger.exception("Unexpected error in create_simulation")
        raise WorkbookAPIError(
            code="INTERNAL_ERROR",
            message="서버 내부 오류",
            status_code=500,
            details=str(e)
        )
