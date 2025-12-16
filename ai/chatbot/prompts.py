from textwrap import dedent
def build_chatbot_prompt(question: str, user_info: dict, conversation_history: list = None) -> str:
    """
    Build chatbot prompt without document retrieval context
    Args:
        question: User's current question
        user_info: User profile information
        conversation_history: List of previous Q&A pairs [{"question": str, "answer": str}, ...]
    Returns:
        str: Formatted prompt for OpenAI
    """
    # informations from user_info dict
    kids_nickname = user_info.get("kids_nickname", "")
    kids_age = user_info.get("kids_age", "")
    kids_tendency = user_info.get("kids_tendency", "")
    kids_note = user_info.get("kids_note", "")
    goal = user_info.get("goal", "")
    worry = user_info.get("worry", "")
    # Build conversation history section
    history_section = ""
    if conversation_history:
        history_lines = ["[이전 대화 내역]"]
        for i, item in enumerate(conversation_history, start=1):
            history_lines.append(f"Q{i}: {item['question']}")
            if item['answer']:
                history_lines.append(f"A{i}: {item['answer']}")
        history_section = "\n".join(history_lines) + "\n\n"
    # prompt
    prompt = f"""
    [기본 지침]
    1) 강요하지 않고 선택지를 제시하세요.
    2) 공감적이고 따뜻한 어투를 유지하세요.
    3) 의학/진단 등 전문 판단이 필요한 내용은 피하고, 필요한 경우 전문가 상담을 권유하세요. (단, 권유 너무 반복 X)
    4) 아이의 발달 단계와 성향을 반영한 실천 가능한 조언을 제시하세요.
    5) 필요한 경우, 위에 정리된 사용자 정보를 반영하여 사용자 정보가 반영됨을 "자연스럽게" 느낄 수 있도록 하세요. (단, 너무 반복 X)
    
    [사용자 정보]
    - 아이 닉네임: {kids_nickname}
    - 아이 나이: {kids_age}
    - 아이 성격: {kids_tendency}
    - 아이 추가 정보: {kids_note}
    - 양육 목표: {goal}
    - 양육 걱정: {worry}
    
    {history_section}
    
    [현재 질문]
    {question}
    
    [중요 지침]
    지난 대화내역과 이어지는 질문을 통해 맥락을 파악해 같은 말을 반복하지 않도록 주의하고, 
    지난 대화내역을 고려해 맥락에서 벗어나지 않지만 !!현재 질문!!에 대한 답변을 작성하세요.
    """
    return dedent(prompt).strip()
def build_chatbot_prompt_with_context(question: str, user_info: dict, contexts: list, conversation_history: list = None) -> str:
    """
    Build a prompt that includes retrieved document contexts (list of strings).
    contexts: list of textual chunks (ordered by relevance)
    conversation_history: List of previous Q&A pairs [{"question": str, "answer": str}, ...]
    """
    base = build_chatbot_prompt(question, user_info, conversation_history)
    # Build contexts block with clear separators
    ctx_lines = ["[참고 자료]"]
    for i, c in enumerate(contexts, start=1):
        ctx_lines.append(f"-- 문서 {i} --")
        ctx_lines.append(c)
    ctx_block = "\n".join(ctx_lines)
    # Instruct model to prioritize using the provided references when relevant
    extra_instruction = "\n\n" + dedent(
        """
        [참고 문서 관련 답변 지침]
        위의 참고 자료는 육아 관련 서적으로부터 답변에 필요한 문단을 추출한 일부 내용입니다.
        소설과 같은 서적이기때문에 사용자 질문에 대해 답변을 생성할 때 육아 방법론적인 부분에서만 참고용으로 활용하되,
        사용자와는 무관한 내용이므로 사용자 정보는 위에 정리된 사용자 정보만을 사용하세요.
        또한, 전문 자료로부터 기반한 답변임을 가볍게 언급하세요. (단, 언급 너무 반복 X)
        당신은 초보 부모를 위한 조언형 챗봇입니다.
        위 모든 답변지침을 지키며, 꼭 2-3문장 내로 !!간결하게!! 작성하며 한 문장이 길어지지 않도록 주의하세요.
        """
    ).strip()
    combined = f"{base}\n\n{ctx_block}\n{extra_instruction}"
    return dedent(combined).strip()