# app.py
from __future__ import annotations

from fastapi import FastAPI, UploadFile, File, Form, HTTPException, Request, Header
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from pathlib import Path
from typing import Optional
from dotenv import load_dotenv
import uuid
import json

# env
load_dotenv()

from config import WORK_DIR
from user_profiles import load_user_profile
from prompts import build_voice_report_json_prompt
from get_client import get_client, request_gpt_for_final
from review_transcript import process_audio_for_review 

app = FastAPI(title="VoiceReport API", version="2.0.0")

MAX_SECONDS = 90.0


# ---------------- Error Standardization ---------------- #
class VoiceReportAPIError(Exception):
    """Custom standardized error for VoiceReport."""
    def __init__(self, code: str, message: str, status_code: int = 400, details=None):
        self.code = code
        self.message = message
        self.status_code = status_code
        self.details = details


@app.exception_handler(VoiceReportAPIError)
async def vr_api_error_handler(request: Request, exc: VoiceReportAPIError):
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "success": False,
            "error": {
                "code": exc.code,
                "message": exc.message,
                "details": exc.details,
            },
        },
    )


@app.exception_handler(HTTPException)
async def http_exc_handler(request: Request, exc: HTTPException):
    if exc.status_code == 422:
        return JSONResponse(status_code=exc.status_code, content={"detail": exc.detail})
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "success": False,
            "error": {"code": "HTTP_ERROR", "message": str(exc.detail), "details": None},
        },
    )


# ---------------- Response Models (문서용) ---------------- #
class Expression(BaseModel):
    parentExpression: str
    kidExpression: str
    parentConditions: str
    kidConditions: str
    expressionFeedback: str


class ChangeProposalItem(BaseModel):
    existingExpression: str
    proposalExpression: str


class EmotionItem(BaseModel):
    time: str
    momentEmotion: str


class Emotion(BaseModel):
    timeline: list[EmotionItem]
    emotionFeedback: str


class Frequency(BaseModel):
    parentFrequency: int
    kidFrequency: int
    frequencyFeedback: str


class VoiceReport(BaseModel):
    reportId: int
    subTitle: str
    day: str
    conversationSummary: str
    overallFeedback: str
    expression: Expression
    changeProposal: list[ChangeProposalItem]
    emotion: Emotion
    kidAttitude: str
    frequency: Frequency
    strength: str


# ---------------- Utilities ---------------- #
def _safe_json_parse(text: str) -> dict:
    """Parse strict JSON; tolerate ```json fences."""
    s = text.strip()
    if s.startswith("```"):
        s = s.strip("`")
        if "\n" in s:
            s = s.split("\n", 1)[1]
    try:
        return json.loads(s)
    except Exception:
        start = s.find("{")
        end = s.rfind("}")
        if start != -1 and end != -1 and end > start:
            cand = s[start : end + 1]
            return json.loads(cand)
        raise


def _get_audio_duration_seconds(audio_path: str) -> float:
    """WAV duration via stdlib."""
    try:
        import wave
        import contextlib
        with contextlib.closing(wave.open(audio_path, "rb")) as wf:
            frames = wf.getnframes()
            rate = wf.getframerate()
            return frames / float(rate)
    except Exception as e:
        print(f"[ERROR] Failed to get audio duration via wave: {e}")
        raise VoiceReportAPIError("FILE_ERROR", "오디오 길이 판독 실패", status_code=500, details=str(e))


# ---------------- Startup & Health ---------------- #
@app.on_event("startup")
def warmup():
    print("[INFO] Starting API warmup...")
    try:
        _ = get_client()
        print("[INFO] OpenAI client initialized")
    except Exception as e:
        print(f"[WARN] OpenAI client warmup failed: {e}")
    print("[INFO] API warmup complete")


@app.get("/")
def root():
    return {"message": "VoiceReport API is running", "version": "2.0.0"}


@app.get("/healthz")
def health_check():
    return {"message": "VoiceReport API is running", "version": "2.0.0", "status": "healthy"}


# ---------------- Main Endpoint ---------------- #
@app.post(
    "/api/voice-report",
    response_model=VoiceReport,
    responses={
        200: {"description": "OK"},
        400: {
            "description": "Audio too long",
            "content": {
                "application/json": {
                    "example": {
                        "success": False,
                        "error": {"code": "AUDIO_TOO_LONG", "message": "오디오 길이 90초 초과", "details": None},
                    }
                }
            },
        },
        404: {
            "description": "User not found",
            "content": {
                "application/json": {
                    "example": {
                        "success": False,
                        "error": {"code": "USER_NOT_FOUND", "message": "사용자 정보를 찾을 수 없습니다.", "details": None}
                    }
                }
            },
        },
        422: {"description": "Validation Error"},
        500: {
            "description": "Internal error",
            "content": {
                "application/json": {
                    "example": {"success": False, "error": {"code": "INTERNAL_ERROR", "message": "서버 내부 오류", "details": None}}
                }
            },
        },
    },
    summary="(Unified) Upload audio (≤90s) and receive structured voice report",
)
def create_voice_report(
    audio: UploadFile = File(...),
    report_id: str = Form(..., alias="reportId"),
    user_id: Optional[str] = Header(None, alias="userId"),
):
    raw_path = None
    try:
        if not user_id:
            raise VoiceReportAPIError("MISSING_HEADER", "userId 헤더가 필요합니다", status_code=400)

        # Save upload
        up_dir = Path(WORK_DIR) / "uploads"
        up_dir.mkdir(parents=True, exist_ok=True)
        uid = uuid.uuid4().hex[:8]
        raw_path = up_dir / f"{uid}_{audio.filename}"
        with raw_path.open("wb") as f:
            f.write(audio.file.read())

        # Duration check
        duration_sec = _get_audio_duration_seconds(str(raw_path))
        if duration_sec > MAX_SECONDS:
            raise VoiceReportAPIError(
                "AUDIO_TOO_LONG",
                f"오디오 길이 {MAX_SECONDS:.0f}초 초과",
                status_code=400,
                details={"duration_sec": round(duration_sec, 2)},
            )

        # OpenAI 기반 STT+diarization → reviewer(내부 구현)
        try:
            client = get_client()
            result = process_audio_for_review(str(raw_path), client=client)
            dialogue = result.get("dialogue")  # "Parent: ...\nChild: ..."
        except VoiceReportAPIError:
            raise
        except Exception as e:
            print(f"[ERROR] Review pipeline failed: {e}")
            raise VoiceReportAPIError("INTERNAL_ERROR", "리뷰 파이프라인 처리 실패", status_code=500, details=str(e))

        # Build prompt → final JSON
        try:
            prof = load_user_profile(user_id)  # KeyError → 404
        except KeyError:
            raise VoiceReportAPIError("USER_NOT_FOUND", "사용자 정보를 찾을 수 없습니다.", status_code=404)

        prompt = build_voice_report_json_prompt(dialogue, prof)
        raw = request_gpt_for_final(prompt)

        try:
            obj = _safe_json_parse(raw)
        except json.JSONDecodeError as e:
            raise VoiceReportAPIError("PARSE_ERROR", "모델 JSON 파싱 실패", status_code=500, details=str(e))

        # Normalize keys
        obj["reportId"] = int(report_id) if report_id.isdigit() else report_id
        obj.pop("id", None)

        # FastAPI가 자동으로 JSON 변환
        return obj

    except VoiceReportAPIError:
        raise
    except FileNotFoundError as e:
        raise VoiceReportAPIError("FILE_ERROR", "업로드 파일 접근 실패", status_code=500, details=str(e))
    except Exception as e:
        raise VoiceReportAPIError("INTERNAL_ERROR", "서버 내부 오류", status_code=500, details=str(e))
    finally:
        if raw_path:
            try:
                raw_path.unlink(missing_ok=True)
            except Exception:
                pass
