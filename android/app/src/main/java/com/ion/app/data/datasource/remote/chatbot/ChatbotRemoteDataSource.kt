package com.ion.app.data.datasource.remote.chatbot

import com.ion.app.data.dto.response.chatbot.ChatbotListDto

interface ChatbotRemoteDataSource {
    suspend fun sendQuestion(question: String): Pair<Long, String>
    suspend fun getChatList(): List<ChatbotListDto>
    suspend fun closeSessionIfNeeded()
}
