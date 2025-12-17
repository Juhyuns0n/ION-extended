package com.ion.app.presentation.chatbot

import com.ion.app.domain.model.chatbot.ChatMessage
import com.ion.app.domain.model.chatbot.ChatSession

data class ChatUiState(
    val conversationId: Long? = null,
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val errorMessage: String? = null
)

data class ChatHistoryUiState(
    val sessions: List<ChatSession> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
