package com.ion.app.domain.repository.chatbot

import com.ion.app.domain.model.chatbot.ChatMessage
import com.ion.app.domain.model.chatbot.ChatSession

interface ChatRepository {
    suspend fun getChatList(): Result<List<ChatSession>>
    suspend fun sendChat(conversationId: Long?, userText: String): Result<ChatMessage> // 서버 응답(봇 메시지)
    suspend fun persistSnapshot(conversationId: Long?, messages: List<ChatMessage>): Result<Unit>
    suspend fun closeSession(): Result<Unit>
}