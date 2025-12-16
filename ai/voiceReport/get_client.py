# get_client.py
from __future__ import annotations

import os
import threading
from typing import Optional, Any, Dict

from openai import OpenAI

from config import (
    OPENAI_API_KEY,
    OPENAI_MODEL,
    OPENAI_MAX_TOKENS,
)

_client: Optional[OpenAI] = None
_client_lock = threading.Lock()


def _resolve_api_key(passed: Optional[str]) -> str:
    key = passed or OPENAI_API_KEY or os.getenv("OPENAI_API_KEY", "")
    if not key:
        raise RuntimeError("OPENAI_API_KEY not set (env/config).")
    return key


def get_client(api_key: Optional[str] = None) -> OpenAI:
    """Return a singleton OpenAI client (thread-safe)."""
    global _client
    if _client is not None:
        return _client
    with _client_lock:
        if _client is None:
            key = _resolve_api_key(api_key)
            _client = OpenAI(api_key=key)
    return _client


def request_gpt(
    prompt: str,
    *,
    model: Optional[str] = None,
    max_completion_tokens: Optional[int] = None,
    system_message: Optional[str] = None,
    extra_kwargs: Optional[Dict[str, Any]] = None,
) -> str:
    """Chat completion → text. Project defaults come from config but can be overridden."""
    client = get_client()
    model = model or OPENAI_MODEL
    max_completion_tokens = OPENAI_MAX_TOKENS if max_completion_tokens is None else int(max_completion_tokens)

    messages = []
    if system_message:
        messages.append({"role": "system", "content": system_message})
    messages.append({"role": "user", "content": prompt})

    kwargs: Dict[str, Any] = {
        "model": model,
        "messages": messages,
        "max_completion_tokens": max_completion_tokens
    }
    if extra_kwargs:
        kwargs.update(extra_kwargs)

    resp = client.chat.completions.create(**kwargs)
    content = getattr(resp.choices[0].message, "content", None) or ""
    return content.strip()


def request_gpt_for_review(prompt: str, **kwargs) -> str:
    """Reviewer flow wrapper."""
    default_system = "스크립트 검수 담당자 역할: 문장 흐름, 논리, 안전성(유해성) 체크를 수행하라."
    return request_gpt(
        prompt,
        model=kwargs.pop("model", "gpt-4.1-2025-04-14"),
        max_completion_tokens=kwargs.pop("max_completion_tokens", OPENAI_MAX_TOKENS),
        system_message=kwargs.pop("system_message", default_system),
        extra_kwargs=kwargs or None,
    )


def request_gpt_for_final(prompt: str, **kwargs) -> str:
    """Final JSON-only wrapper."""
    default_system = "최종 출력은 반드시 유효한 JSON만 출력하라. 다른 텍스트는 포함하지 마라."
    return request_gpt(
        prompt,
        model=kwargs.pop("model", "gpt-4.1-2025-04-14"),
        max_completion_tokens=kwargs.pop("max_completion_tokens", OPENAI_MAX_TOKENS),
        system_message=kwargs.pop("system_message", default_system),
        extra_kwargs=kwargs or None,
    )


def request_transcription(
    audio_file_path: str,
    *,
    model: str = "gpt-4o-transcribe-diarize",
    language: Optional[str] = None,
    response_format: str = "diarized_json",
    chunking_strategy: str = "auto",
    **extra_kwargs,
) -> Any:
    """Audio diarized transcription helper."""
    client = get_client()
    with open(audio_file_path, "rb") as f:
        kwargs: Dict[str, Any] = {
            "model": model,
            "file": f,
            "response_format": response_format,
            "chunking_strategy": chunking_strategy,
        }
        if language:
            kwargs["language"] = language
        kwargs.update(extra_kwargs)
        resp = client.audio.transcriptions.create(**kwargs)
    return resp
