# review_pipeline.py  — OpenAI diarized_json만 사용하는 정리 버전

import os
import argparse
import json
import re
from typing import Any, Dict, List

from get_client import get_client  # 패키지 공용 클라이언트 팩토리


# -------------------- Utilities -------------------- #
def load_api_key() -> str:
    """Load OpenAI API key from environment variable OPENAI_API_KEY."""
    return os.environ.get("OPENAI_API_KEY", "")


def build_review_prompt(transcript: Dict[str, Any]) -> str:
    """검수자 프롬프트(한국어). 입력 transcript(dict) → 모델에게 정제된 segments JSON만 반환하게 지시."""
    example_segment = {
        "Parent": "Parent",
        "text": "안녕하세요. 오늘은 뭐하고 놀래?",
        "start": 0.0,
        "end": 2.3,
    }

    prompt = (
        "다음은 자동 음성 인식 및 화자 분리 모델의 출력입니다.\n"
        "당신의 역할: '검수자'입니다. 목적은 화자 분리의 정확성을 확인하고, 필요하면 대화 맥락을 파악해 화자를 'Parent'와 'Child'로 나눕니다.\n"
        "대화 문장이 흐트러지더라도 내용을 수정하지 마세요.\n\n"
        "출력 규칙(엄격하게 지켜주세요):\n"
        "1) 반드시 JSON 하나만 출력하세요. 최상위 키는 'segments'로 하고, 값은 segment 객체의 배열입니다.\n"
        "2) 각 segment 객체는 정확히 다음 키를 포함해야 합니다: Parent, text, start, end.\n"
        "   - Parent: 'Parent' 또는 'Child' 중 하나로 라벨링하세요.\n"
        "   - text: 대화문 내용, 수정금지.\n"
        "   - start, end: 초 단위 실수(예: 1.23, 소수점 둘째자리까지).\n"
        "3) 화자 구분이 확실하지 않으면 문맥(대화의 내용, 친밀도, 발화 길이 등)을 이용해 Parent/Child로 구분하세요.\n"
        "4) 원래 segment의 start/end 타임은 보존하되, 인접한 짧은 발화를 합쳐야 의미가 더 명확해지면 합쳐도 됩니다.\n"
        "입력(딕셔너리 형태):\n"
        + json.dumps(transcript, ensure_ascii=False, indent=2)
        + "\n\n예시 segment 형식:\n"
        + json.dumps({"segments": [example_segment]}, ensure_ascii=False, indent=2)
    )
    return prompt


def get_response_text(resp: Any) -> str:
    """OpenAI responses.create 응답에서 텍스트를 최대한 견고하게 추출."""
    if isinstance(resp, dict):
        if "output_text" in resp and isinstance(resp["output_text"], str):
            return resp["output_text"]
        out = resp.get("output") or resp.get("choices")
        if isinstance(out, list) and len(out) > 0:
            texts = []
            for o in out:
                if isinstance(o, dict):
                    for k in ("text", "content", "message"):
                        v = o.get(k)
                        if isinstance(v, str):
                            texts.append(v)
                        elif isinstance(v, list):
                            for item in v:
                                if isinstance(item, dict) and "text" in item:
                                    texts.append(item["text"])
            if texts:
                return "\n".join(texts)
    else:
        try:
            d = resp.model_dump()
            return get_response_text(d)
        except Exception:
            pass
    return str(resp)


def extract_json_from_text(text: str) -> Dict[str, Any]:
    """모델 텍스트 출력에서 JSON을 최대한 안전하게 추출."""
    text = text.strip()
    try:
        return json.loads(text)
    except Exception:
        pass

    m = re.search(r"(\{(?:.|\n)*\})", text)
    if m:
        cand = m.group(1)
        try:
            return json.loads(cand)
        except Exception:
            pass

    m = re.search(r"(\[(?:.|\n)*\])", text)
    if m:
        cand = m.group(1)
        try:
            return {"segments": json.loads(cand)}
        except Exception:
            pass

    raise ValueError("모델 응답에서 JSON을 추출할 수 없습니다. 응답 원문:\n" + text)


def review_with_model(transcript: Dict[str, Any], client: Any, model: str = "gpt-5-mini-2025-08-07") -> Dict[str, Any]:
    """검수자(Text) 모델로 프롬프트를 보내고 JSON 파싱."""
    prompt = build_review_prompt(transcript)
    resp = client.responses.create(model=model, input=prompt)
    resp_text = get_response_text(resp)
    parsed = extract_json_from_text(resp_text)
    return parsed


def pretty_print_segments(segments: List[Dict[str, Any]]) -> None:
    for seg in segments:
        spk = seg.get("Parent", "Child")
        text = seg.get("text", "")
        start = seg.get("start", 0.0)
        end = seg.get("end", 0.0)
        print(f"[{spk}] {text} ({start:.2f}-{end:.2f}s)")


def process_audio_for_review(
    audio_path: str,
    client: Any = None,
    known_speakers: List[str] | None = None,
) -> Dict[str, Any]:
    """
    OpenAI STT(diarized_json) → 검수자 모델 → Parent/Child 대화 문자열 구성.
    반환:
    {
      "dialogue": "Parent: ...\nChild: ...",
      "reviewed": {"segments": [...]},
      "raw_transcript": {...}  # STT 원본(diarized_json)
    }
    """
    if client is None:
        client = get_client()

    extra_body = {}
    if known_speakers:
        # 이름만 넣으면 references도 같이 요구하는 최신 API일 수 있으므로
        # references가 없다면 names는 생략하는 것이 안전하다.
        # 여기서는 names 미사용(참조 음성 없다고 가정).
        pass

    # 1) OpenAI diarized transcription
    with open(audio_path, "rb") as f:
        t = client.audio.transcriptions.create(
            model="gpt-4o-transcribe-diarize",
            file=f,
            response_format="diarized_json",
            chunking_strategy="auto",
            # extra_body=extra_body,  # 참조 음성이 있을 때만 사용 권장
        )
    transcript = t if isinstance(t, dict) else t.model_dump()

    # 2) 검수자 모델로 정제(JSON 강제)
    reviewed = review_with_model(transcript, client)

    # 3) Parent/Child 대사 라인 생성
    lines = []
    for seg in reviewed.get("segments", []):
        parent = seg.get("Parent") or seg.get("parent") or seg.get("label") or "Child"
        speaker_label = "Parent" if str(parent).lower().startswith("parent") else "Child"
        text = str(seg.get("text", "")).strip()
        if not text:
            continue
        lines.append(f"{speaker_label}: {text}")
    dialogue = "\n".join(lines)

    return {"dialogue": dialogue, "reviewed": reviewed, "raw_transcript": transcript}


# -------------------- CLI main -------------------- #
def main():
    parser = argparse.ArgumentParser(description="OpenAI diarization + reviewer pipeline")
    parser.add_argument("--audio", "-a", help="오디오 파일 경로")
    parser.add_argument("--from-json", "-j", help="이미 존재하는 diarized_json 파일 경로(전사 스킵)")
    parser.add_argument("--dry-run", action="store_true", help="프롬프트만 출력(실제 API 호출 안 함)")
    args = parser.parse_args()

    api_key = load_api_key()
    if not api_key:
        print("경고: 환경변수 OPENAI_API_KEY가 설정되어 있지 않습니다. 실제 API 호출은 실패할 수 있습니다.")

    client = None
    if not args.dry_run:
        client = get_client()

    # 전사 입력 확보
    if args.from_json:
        with open(args.from_json, "r", encoding="utf-8") as f:
            transcript = json.load(f)
    elif args.audio:
        if args.dry_run:
            # 프롬프트 확인용 가짜 전사
            transcript = {"segments": [{"Parent": "Unknown", "text": "[audio segments omitted]", "start": 0.0, "end": 1.0}]}
        else:
            # 실제 파이프라인 수행
            result = process_audio_for_review(args.audio, client=client)
            print(json.dumps(result["reviewed"], ensure_ascii=False, indent=2))
            pretty_print_segments(result["reviewed"].get("segments", []))
            return
    else:
        parser.error("--audio 또는 --from-json 중 하나를 지정하거나 --dry-run을 사용하세요.")

    # dry-run 또는 from-json → 검수자만 수행
    if args.dry_run:
        prompt = build_review_prompt(transcript)
        print("--- Generated review prompt (truncated) ---")
        print(prompt[:4000])
        return

    reviewed = review_with_model(transcript, client)
    print(json.dumps(reviewed, ensure_ascii=False, indent=2))
    pretty_print_segments(reviewed.get("segments", []))


if __name__ == "__main__":
    main()
