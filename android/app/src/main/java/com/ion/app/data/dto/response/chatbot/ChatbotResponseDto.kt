package com.ion.app.data.dto.response.chatbot

import kotlinx.serialization.Serializable

@Serializable
data class AnswerResponseDto(
    val sessionId: Long,
    val answer: String
)

@Serializable
data class ChatErrorResponseDto(
    val code: String,
    val message: String,
    val path: String
)

@Serializable
data class ChatbotListDto(
    val sessionId: Long,
    val questions: List<QuestionAnswerDto>
)

@Serializable
data class QuestionAnswerDto(
    val question: String,
    val answer: String
)