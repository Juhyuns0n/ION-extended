package com.ion.app.data.repositoryimpl.chatbot

import android.util.Log
import com.ion.app.data.datasource.remote.chatbot.ChatbotRemoteDataSource
import com.ion.app.data.datasourceimpl.local.ChatbotLocalDataSource
import com.ion.app.data.mapper.chatbot.toDomain
import com.ion.app.domain.model.chatbot.ChatMessage
import com.ion.app.domain.model.chatbot.ChatSession
import com.ion.app.domain.repository.chatbot.ChatRepository
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val localDataSource: ChatbotLocalDataSource,
    private val remoteDataSource: ChatbotRemoteDataSource
) : ChatRepository {

    override suspend fun getChatList(): Result<List<ChatSession>> =
        runCatching {
            val dto = remoteDataSource.getChatList()
            dto.map { it.toDomain() }
        }

    override suspend fun sendChat(
        conversationId: Long?,
        userText: String
    ): Result<ChatMessage> =
        runCatching {
            // 사용자 메시지를 대화에 추가 (히스토리용)
            localDataSource.addUserMessage(conversationId, userText)

            // 서버에 question 전송 → (sessionId, answer) 반환
            val (_, answer) = remoteDataSource.sendQuestion(userText)

            // answer를 ChatMessage로 변환
            val botMsg = ChatMessage(
                id = System.currentTimeMillis(),
                text = answer,
                isUser = false
            )

            // 히스토리에 챗봇 메시지도 추가
            localDataSource.addBotMessage(botMsg)

            botMsg
        }.onFailure { e ->
            Log.e("ChatRepository", "sendChat failed", e)
        }

    override suspend fun persistSnapshot(
        conversationId: Long?,
        messages: List<ChatMessage>
    ): Result<Unit> =
        runCatching {
            localDataSource.saveSnapshot(conversationId, messages)
        }.onFailure { e ->
            Log.e("ChatRepository", "persistSnapshot failed", e)
        }

    override suspend fun closeSession(): Result<Unit> =
        runCatching {
            remoteDataSource.closeSessionIfNeeded()
        }.onFailure { e ->
            Log.e("ChatRepository", "closeSession failed", e)
        }
}
