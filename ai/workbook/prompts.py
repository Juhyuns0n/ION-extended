from textwrap import dedent

def build_workbook_prompt(user_info: dict, chapter_info: dict, lesson_id: int) -> str:
    """
    워크북 생성을 위한 프롬프트 작성
    
    Args:
        user_info: 사용자 정보 (kids_nickname, kids_age, kids_tendency, etc.)
        chapter_info: 챕터 정보 (chapter_name, necessity, study_goal, notion)
        lesson_id: 레슨 번호 (1-4)
        
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
    
    # 챕터 정보 추출
    chapter_name = chapter_info.get("chapter_name", "")
    necessity = chapter_info.get("necessity", "")
    study_goal = chapter_info.get("study_goal", "")
    notion = chapter_info.get("notion", "")
    
    schema = """
    {
      "chapterTitle": String,
      "lessonTitle": String,
      "firstDescriptiveFormQuestion": String,
      "firstDescriptiveFormExample": String,
      "secondDescriptiveFormQuestion": String,
      "secondDescriptiveFormExample": String,
      "firstSelectiveQuestion": String,
      "firstSelectiveOptions": [String, String, String, String],
      "firstSelectiveExample": String,
      "secondSelectiveQuestion": String,
      "secondSelectiveOptions": [String, String, String, String],
      "secondSelectiveExample": String,
      "thirdSelectiveQuestion": String,
      "thirdSelectiveOptions": [String, String, String, String],
      "thirdSelectiveExample": String
    }
    """.strip()
    
    inst = f"""
    너는 부모 교육 워크북을 생성하는 전문가다.
    아래의 '사용자 정보'와 '챕터 정보'를 참고하여, 위 SCHEMA에 맞는 완전한 JSON만 출력하라.

    [필수 규칙]
    - chapterTitle에는 "{chapter_name}"을 그대로 사용한다.
    - lessonTitle은 챕터의 내용과 학습 목표를 반영하여, {lesson_id}번째 레슨에 적합한 제목을 생성한다.
    - 레슨 제목은 해당 챕터의 세부 주제나 학습 단계를 나타내야 한다. (예: "공감적 경청의 기초", "감정 코칭 실전 적용" 등)
    - 서술형 문제는 2개를 생성한다. 각 문제는 부모가 실제 상황에 적용할 수 있는 실용적인 질문이어야 한다.
    - 서술형 문제는 너무 광범위하지 않게 모범 답안이 하나로 집중될 수 있는 질문이어야 한다.
    - 서술형 문제는 한 문장으로 간결하게 작성한다.
    - 서술형 모범 답안은 각각 구체적이고 실행 가능한 방법을 제시하지만, 너무 길지 않게 2-3문장 이내로 작성한다.
    - 선택형 문제는 3개를 생성한다. 각 문제는 정답이 명확하게 존재하지만 너무 쉽거나 직관적이지 않은 생각해볼만한 질문이어야 한다.
    - 선택형 옵션은 각 문제마다 반드시 4개이며, 그 중 하나가 정답이다.
    - firstSelectiveExample, secondSelectiveExample, thirdSelectiveExample에는 각각 정답 옵션의 전체 텍스트를 넣는다.
    - 사용자의 아이 정보(나이, 성향, 걱정 등)를 반영하여 맞춤형 문제를 생성한다.
    - 챕터의 학습 목표와 핵심 개념을 반영한 문제를 만든다.
    - 오직 유효한 JSON만 출력한다. 마크다운, 백틱, 주석, 설명 문장 금지.
    - 모든 값은 한국어로 작성한다.
    
    [사용자 정보]
    - 아이 닉네임: {kids_nickname}
    - 아이 나이: {kids_age}
    - 아이 성격: {kids_tendency}
    - 아이 추가 정보: {kids_note}
    - 양육 목표: {goal}
    - 양육 걱정: {worry}
    
    [챕터 정보]
    - 챕터명: {chapter_name}
    - 필요성: {necessity}
    - 학습 목표: {study_goal}
    - 핵심 개념: {notion}
    - 현재 레슨 번호: {lesson_id} (전체 4개 레슨 중 {lesson_id}번째)
    
    [출력 형식]
    반드시 아래 SCHEMA에 맞춘 순수 JSON만 출력:
    {schema}
    
    ** lessonTitle은 챕터 내용을 기반으로 {lesson_id}번째 레슨에 맞는 구체적이고 의미있는 제목을 생성하라. **
    """
    
    return dedent(inst).strip()


def build_simulation_prompt(user_info: dict, chapter_info: dict, dialogues: list = None) -> str:
    """
    시뮬레이션 워크북 생성을 위한 프롬프트 작성
    
    Args:
        user_info: 사용자 정보 (kids_nickname, kids_age, kids_tendency, etc.)
        chapter_info: 챕터 정보 (chapter_name, necessity, study_goal, notion)
        dialogues: 이전 대화 내역 [{"userLine": str, "aiLine": str}, ...] 또는 None
        
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
    
    # 챕터 정보 추출
    chapter_name = chapter_info.get("chapter_name", "")
    necessity = chapter_info.get("necessity", "")
    study_goal = chapter_info.get("study_goal", "")
    notion = chapter_info.get("notion", "")
    
    # 대화 내역 구성
    dialogue_section = ""
    if dialogues:
        dialogue_lines = ["[이전 대화 내역]"]
        for idx, dialogue in enumerate(dialogues, start=1):
            user_line = dialogue.get("userLine", "")
            ai_line = dialogue.get("aiLine", "")
            if user_line:
                dialogue_lines.append(f"부모: {user_line}")
            if ai_line:
                dialogue_lines.append(f"아이: {ai_line}")
        dialogue_section = "\n".join(dialogue_lines) + "\n\n"
    
    schema = """
    {
      "chapterTitle": String,
      "lessonTitle": String,
      "simulationSituationExplain": String,
      "aiLine": String
    }
    """.strip()
    
    inst = f"""
    너는 부모-자녀 대화 시뮬레이션을 위해 아이 역할을 맡았다.
    아래의 '사용자 정보'와 '챕터 정보'를 참고하여, 위 SCHEMA에 맞는 완전한 JSON만 출력하라.

    [필수 규칙]
    - chapterTitle에는 "{chapter_name}"을 그대로 사용한다.
    - lessonTitle은 "시뮬레이션: " 접두사와 함께 챕터 내용에 맞는 실전 상황 제목을 생성한다. (예: "시뮬레이션: 아이의 짜증 상황 대응하기")
    - simulationSituationExplain은 현재 시뮬레이션 상황을 한 문장으로 핵심적으로 설명한다.
    - 아이의 나이({kids_age})와 성격({kids_tendency})에 맞는 말투와 표현을 사용한다.
    - 챕터의 학습 목표를 실천할 수 있는 상황을 설정한다.
    - 오직 유효한 JSON만 출력한다. 마크다운, 백틱, 주석, 설명 문장 금지.
    - 모든 값은 한국어로 작성한다.
    - 아이다운 말투와 표현을 사용한다.
    
    ** 중요: 이전 대화 처리 규칙 **
    - 이전 대화가 없으면: 새로운 일상 상황으로 시작한다.
    - 이전 대화가 있으면: 
      1) 반드시 이전 대화의 "마지막 부모 발화"에 직접적으로 반응한다.
      2) 대화의 흐름과 맥락을 이어간다.
      3) 부모의 말에 대한 자연스러운 아이의 반응을 생성한다.
      4) 완전히 새로운 주제로 바꾸거나 기존 대화를 반복하지 않는다. 
    
    [사용자 정보]
    - 아이 닉네임: {kids_nickname}
    - 아이 나이: {kids_age}
    - 아이 성격: {kids_tendency}
    - 아이 추가 정보: {kids_note}
    - 양육 목표: {goal}
    - 양육 걱정: {worry}
    
    [챕터 정보]
    - 챕터명: {chapter_name}
    - 필요성: {necessity}
    - 학습 목표: {study_goal}
    - 핵심 개념: {notion}
    
    {dialogue_section}[출력 형식]
    반드시 아래 SCHEMA에 맞춘 순수 JSON만 출력:
    {schema}
    
    ** 핵심 지시사항 **
    1. 이전 대화가 있더라도, 마지막 부모의 말("{dialogues[-1].get('userLine', '') if dialogues else ''}")에 대한 아이의 직접적인 반응을 aiLine에 생성하라.
    2. 아이의 나이({kids_age})와 성격({kids_tendency})에 맞게 자연스럽게 대답하라.
    3. 대화의 맥락과 흐름을 유지하며, 갑자기 주제를 바꾸지 마라.
    4. 아이답게 솔직하고 자연스러운 반응을 보여라.
    5. 아이의 대답은 간결하게(10글자 전후) 생성해라. 
    """
    
    return dedent(inst).strip()
