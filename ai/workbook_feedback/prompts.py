from textwrap import dedent

def build_workbook_feedback_prompt(
    user_info: dict,
    first_descriptive_question: str,
    first_descriptive_answer: str,
    first_descriptive_example: str,
    second_descriptive_question: str,
    second_descriptive_answer: str,
    second_descriptive_example: str,
    first_selective_question: str,
    first_selective_options: list,
    first_selective_answer: str,
    first_selective_example: str,
    second_selective_question: str,
    second_selective_options: list,
    second_selective_answer: str,
    second_selective_example: str,
    third_selective_question: str,
    third_selective_options: list,
    third_selective_answer: str,
    third_selective_example: str
) -> str:
    """
    워크북 피드백 생성을 위한 프롬프트 작성
    
    Args:
        user_info: 사용자 정보
        first_descriptive_question: 첫 번째 서술형 문제
        first_descriptive_answer: 사용자가 입력한 첫 번째 서술형 답안
        first_descriptive_example: 첫 번째 서술형 모범 답안
        second_descriptive_question: 두 번째 서술형 문제
        second_descriptive_answer: 사용자가 입력한 두 번째 서술형 답안
        second_descriptive_example: 두 번째 서술형 모범 답안
        first_selective_question: 첫 번째 선택형 문제
        first_selective_options: 첫 번째 선택형 옵션들
        first_selective_answer: 첫 번째 선택형 정답
        first_selective_example: 사용자가 선택한 첫 번째 답
        second_selective_question: 두 번째 선택형 문제
        second_selective_options: 두 번째 선택형 옵션들
        second_selective_answer: 두 번째 선택형 정답
        second_selective_example: 사용자가 선택한 두 번째 답
        third_selective_question: 세 번째 선택형 문제
        third_selective_options: 세 번째 선택형 옵션들
        third_selective_answer: 세 번째 선택형 정답
        third_selective_example: 사용자가 선택한 세 번째 답
        
    Returns:
        str: GPT에 전달할 프롬프트
    """
    # 사용자 정보 추출
    kids_nickname = user_info.get("kids_nickname", "")
    kids_age = user_info.get("kids_age", "")
    kids_tendency = user_info.get("kids_tendency", "")
    kids_note = user_info.get("kids_note", "")
    goal = user_info.get("goal", "")
    worry = user_info.get("worry", "")
    
    # 선택형 옵션 문자열화
    first_options_str = "\n".join([f"  {i+1}. {opt}" for i, opt in enumerate(first_selective_options)])
    second_options_str = "\n".join([f"  {i+1}. {opt}" for i, opt in enumerate(second_selective_options)])
    third_options_str = "\n".join([f"  {i+1}. {opt}" for i, opt in enumerate(third_selective_options)])
    
    schema = """
    {
      "workbookFeedback": String
    }
    """.strip()
    
    inst = f"""
    너는 부모 교육 워크북 피드백을 제공하는 전문가다.
    아래의 정보를 참고하여, 사용자의 답변에 대한 피드백을 생성하라.

    [필수 규칙]
    - 서술형(2개)과 선택형(3개) 총 5개 문제에 대한 종합 피드백을 제공한다.
    - 사용자의 답변이 좋은 점을 먼저 언급한다.
    - 개선이 필요한 부분은 구체적이고 실천 가능한 방법으로 제시한다.
    - 사용자의 아이 정보(나이, 성향, 걱정)를 반영한 맞춤형 피드백을 제공한다.
    - 너무 길지 않게 핵심적으로 간결하게 작성한다. (3-5문장)
    - 격려와 응원의 톤을 유지한다.
    - 오직 유효한 JSON만 출력한다. 마크다운, 백틱, 주석, 설명 문장 금지.
    - 모든 값은 한국어로 작성한다.
    
    [사용자 정보]
    - 아이 닉네임: {kids_nickname}
    - 아이 나이: {kids_age}
    - 아이 성격: {kids_tendency}
    - 아이 추가 정보: {kids_note}
    - 양육 목표: {goal}
    - 양육 걱정: {worry}
    
    [첫 번째 서술형 문제]
    문제: {first_descriptive_question}
    사용자 답변: {first_descriptive_answer}
    모범 답안: {first_descriptive_example}
    
    [두 번째 서술형 문제]
    문제: {second_descriptive_question}
    사용자 답변: {second_descriptive_answer}
    모범 답안: {second_descriptive_example}
    
    [첫 번째 선택형 문제]
    문제: {first_selective_question}
    선택지:
{first_options_str}
    정답: {first_selective_answer}
    사용자 선택: {first_selective_example}
    
    [두 번째 선택형 문제]
    문제: {second_selective_question}
    선택지:
{second_options_str}
    정답: {second_selective_answer}
    사용자 선택: {second_selective_example}
    
    [세 번째 선택형 문제]
    문제: {third_selective_question}
    선택지:
{third_options_str}
    정답: {third_selective_answer}
    사용자 선택: {third_selective_example}
    
    [출력 형식]
    반드시 아래 SCHEMA에 맞춘 순수 JSON만 출력:
    {schema}
    
    ** workbookFeedback에는 서술형 2개와 선택형 3개 문제에 대한 종합 피드백을 간결하게(3-5문장) 작성하라. **
    """
    
    return dedent(inst).strip()


def build_simulation_feedback_prompt(user_info: dict, dialogues: list) -> str:
    """
    시뮬레이션 피드백 생성을 위한 프롬프트 작성
    
    Args:
        user_info: 사용자 정보
        dialogues: 대화 내역 [{"userLine": str, "aiLine": str}, ...]
        
    Returns:
        str: GPT에 전달할 프롬프트
    """
    # 사용자 정보 추출
    kids_nickname = user_info.get("kids_nickname", "")
    kids_age = user_info.get("kids_age", "")
    kids_tendency = user_info.get("kids_tendency", "")
    kids_note = user_info.get("kids_note", "")
    goal = user_info.get("goal", "")
    worry = user_info.get("worry", "")
    
    # 대화 내역 구성
    dialogue_lines = ["[대화 시뮬레이션 내역]"]
    for idx, dialogue in enumerate(dialogues, start=1):
        user_line = dialogue.get("userLine", "")
        ai_line = dialogue.get("aiLine", "")
        if user_line:
            dialogue_lines.append(f"부모: {user_line}")
        if ai_line:
            dialogue_lines.append(f"아이: {ai_line}")
    dialogue_section = "\n".join(dialogue_lines)
    
    schema = """
    {
      "simulationFeedback": String
    }
    """.strip()
    
    inst = f"""
    너는 부모-자녀 대화 시뮬레이션 피드백을 제공하는 전문가다.
    아래의 대화 내역을 분석하여, 부모의 대화 방식에 대한 피드백을 생성하라.

    [필수 규칙]
    - 부모가 말한 발화들(userLine)을 중점적으로 분석한다.
    - 좋았던 대화 방식을 먼저 구체적으로 칭찬한다.
    - 개선이 필요한 부분은 부드럽게 제안한다.
    - 사용자의 아이 정보(나이, 성향, 걱정)를 반영한 맞춤형 피드백을 제공한다.
    - 구체적인 대화 예시를 들어 설명한다.
    - 간결하게 작성한다. (4-6문장)
    - 격려와 응원의 톤을 유지한다.
    - 오직 유효한 JSON만 출력한다. 마크다운, 백틱, 주석, 설명 문장 금지.
    - 모든 값은 한국어로 작성한다.
    
    [사용자 정보]
    - 아이 닉네임: {kids_nickname}
    - 아이 나이: {kids_age}
    - 아이 성격: {kids_tendency}
    - 아이 추가 정보: {kids_note}
    - 양육 목표: {goal}
    - 양육 걱정: {worry}
    
    {dialogue_section}
    
    [출력 형식]
    반드시 아래 SCHEMA에 맞춘 순수 JSON만 출력:
    {schema}
    
    ** simulationFeedback에는 부모의 대화 방식에 대한 구체적이고 실천 가능한 피드백을 간결하게 3문장 이내로 작성하라. **
    """
    
    return dedent(inst).strip()
