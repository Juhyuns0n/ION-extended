package com.ion.app.domain.model.chatbot

data class ChatMessage(
    val id: Long,
    val text: String,
    val isUser: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)

data class ChatSession(
    val sessionId: Long,
    val questions: List<QuestionAnswer>
)

data class QuestionAnswer(
    val question: String,
    val answer: String
)
