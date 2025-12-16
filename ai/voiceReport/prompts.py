from textwrap import dedent

def build_voice_report_json_prompt(dialogue: str, user_info: dict) -> str:
    """
    Instruct GPT to return STRICT JSON in the EXACT schema required.
    """
    kids_nickname = user_info.get("kids_nickname","")
    kids_age = user_info.get("kids_age","")
    kids_tendency = user_info.get("kids_tendency","")
    kids_note = user_info.get("kids_note","")
    goal = user_info.get("goal","")
    worry = user_info.get("worry","")

    schema = """
    {
      "reportId": Integer,
      "subTitle": String,
      "day": String,
      "conversationSummary": String,
      "overallFeedback": String,
      "expression": {
        "parentExpression": String,
        "kidExpression": String,
        "parentConditions": String,
        "kidConditions": String,
        "expressionFeedback": String
      },
      "changeProposal": [
        {"existingExpression": String, "proposalExpression": String},
        {"existingExpression": String, "proposalExpression": String}
      ],
      "emotion": {
        "timeline": [
          {"time": String, "momentEmotion": String},
          {"time": String, "momentEmotion": String}
        ],
        "emotionFeedback": String
      },
      "kidAttitude": String,
      "frequency": {
        "parentFrequency": Integer,
        "kidFrequency": Integer,
        "frequencyFeedback": String
      },
      "strength": String
    }
    """.strip()

    inst = f"""
    너는 부모-아이 대화 코칭 보고서를 생성하는 비서다.
    아래 '대화'와 '사용자 정보'를 참고하여, 위 SCHEMA에 맞는 완전한 JSON만 출력하라.

    [필수 규칙]
    - 대화문에 있는 내용은 STT 추출이 잘못된 경우가 있을 수 있으니, 고유명사의 이름은 언급하지 않고 대화 방식에 집중한다.
    - 부모의 양육 태도나 아이의 특성에 맞춘 조언을 반영한다.
    - 톤과 스타일은 사용자가 선호하는 방식에 맞춘다.
    - 오직 유효한 JSON만 출력한다. 마크다운, 백틱, 주석, 설명 문장 금지.
    - 모든 키를 포함하고, 값은 한국어로 자연스럽게 작성한다.
    - "id"는 1 이상의 정수.
    - "day"는 YYYY-MM-DD 형식.
    - "timeline"의 time은 "mm:ss-mm:ss" 또는 "hh:mm-hh:mm" 등 간단한 범위 문자열.
    - "changeProposal"은 반드시 2개 아이템.
    - "parentConditions" / "kidConditions"는 말의 조건적 구조나 전제(점선 표기용)를 요약.
    - "momentEmotion"은 3글자 이내의 한 단어로 부모의 감정 상태를 표현.
    - JSON은 최소화(minimize)하여 공백을 불필요하게 넣지 말 것.

    [사용자 정보]
    - kids_nickname: {kids_nickname}
    - kids_age: {kids_age}
    - kids_tendency: {kids_tendency}
    - kids_note: {kids_note}
    - goal: {goal}
    - worry: {worry}

    [대화]
    {dialogue}

    [출력 형식]
    반드시 아래 SCHEMA에 맞춘 순수 JSON만 출력:
    {schema}
    """
    return dedent(inst).strip()
