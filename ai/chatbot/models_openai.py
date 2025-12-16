from typing import Optional
from openai import OpenAI
from config import OPENAI_API_KEY, OPENAI_MODEL

_client: Optional[OpenAI] = None

def _get_client() -> OpenAI:
    global _client
    if _client is None:
        if not OPENAI_API_KEY:
            raise RuntimeError("OPENAI_API_KEY not set")
        _client = OpenAI(api_key=OPENAI_API_KEY)
    return _client

def generate_openai_answer(prompt: str) -> str:
    client = _get_client()
    # system message: role set, user measage: prompt
    resp = client.chat.completions.create(
        model=OPENAI_MODEL,
        messages=[
            {"role": "system", "content": "당신은 육아 상담 챗봇입니다."},
            {"role": "user", "content": prompt},
        ],
    )
    content = resp.choices[0].message.content
    return content.strip()
