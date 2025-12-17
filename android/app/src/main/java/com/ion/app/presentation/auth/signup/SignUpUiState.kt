package com.ion.app.presentation.auth.signup

data class SignUpUiState(
    var step: Int = 1,

    // 1단계: ID, PW, 부모정보
    val email: String = "",
    val password: String = "",
    val passwordCheck: String = "",
    val parentName: String = "",
    val parentNickname: String = "",

    // --- 1단계 에러 메시지들 ---
    val passwordError: String? = null,
    val passwordCheckError: String? = null,
    val step1Error: String? = null,

    // --- 3단계 약관 에러 ---
    val termsError: String? = null,
    val toastMessage: String? = null,

    // 2단계: 아이 정보
    val kidsNickname: String = "",
    val kidsAge: String = "",
    val kidsTendency: String = "",
    val kidsNote: String = "",

    // 3단계: 목표, 걱정, 약관 동의
    val goal: String = "",
    val worry: String = "",
    val personalInformationAgree: Boolean = false,

    // 4단계: 부모유형 테스트
    val propensityQuestions: List<PropensityQuestion> = emptyList(),

    // 결과
    val parentTypeResult: ParentTypeResultUiState? = null,
    val userId: String? = null
)

data class ParentTypeResultUiState(
    val userType: String = "",
    val userTypeKo: String = "",    // 타입 한글명
    val description: String = "",   // 타입 설명
    val mainScores: String = "",       // 상위 3타입 점수
    val subScores: List<String> = emptyList(),   // subtype 한국어 + 점수 리스트
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
