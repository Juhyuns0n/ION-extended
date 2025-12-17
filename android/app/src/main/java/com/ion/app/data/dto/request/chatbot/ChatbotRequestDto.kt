package com.ion.app.data.dto.request.chatbot

import kotlinx.serialization.Serializable

@Serializable
data class QuestionRequestDto(
    val question: String
)