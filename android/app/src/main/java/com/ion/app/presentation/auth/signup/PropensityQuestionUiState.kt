package com.ion.app.presentation.auth.signup

data class PropensityQuestion(
    val id: Int,
    val question: String,
    val selectedScore: Int? = null   // 1~5, 아직 선택 안 했으면 null
)
