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

def generate_feedback(prompt: str) -> str:
    """
    Call OpenAI API to generate feedback JSON
    
    Args:
        prompt: The prompt for feedback generation
        
    Returns:
        str: JSON string response from GPT
    """
    client = _get_client()
    
    response = client.chat.completions.create(
        model=OPENAI_MODEL,
        messages=[
            {
                "role": "system",
                "content": "너는 부모 교육 워크북 피드백을 제공하는 전문가다. 항상 유효한 JSON 형식으로만 응답한다."
            },
            {
                "role": "user",
                "content": prompt
            }
        ],
        temperature=0.7,
        max_tokens=1000
    )
    
    content = response.choices[0].message.content
    return content.strip()
