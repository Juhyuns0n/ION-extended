package com.ion.app.data.mapper.chatbot

import com.ion.app.data.dto.response.chatbot.ChatbotListDto
import com.ion.app.domain.model.chatbot.ChatSession
import com.ion.app.domain.model.chatbot.QuestionAnswer

fun ChatbotListDto.toDomain() = ChatSession(
    sessionId = sessionId,
    questions = questions.map {
        QuestionAnswer(question = it.question, answer = it.answer)
    }
)
