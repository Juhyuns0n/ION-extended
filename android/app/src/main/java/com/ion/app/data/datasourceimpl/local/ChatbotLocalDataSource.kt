package com.ion.app.data.datasourceimpl.local

import com.ion.app.domain.model.chatbot.ChatMessage
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.random.Random

class ChatbotLocalDataSource @Inject constructor() {

    // 단일 세션용 메모리 저장
    private val messageCache = mutableListOf<ChatMessage>()

    suspend fun getChatHistory(conversationId: Long?): List<ChatMessage> {
        delay(200)
        return messageCache.toList()
    }

    fun addUserMessage(conversationId: Long?, text: String): ChatMessage {
        val msg = ChatMessage(
            id = Random.nextLong(),
            text = text,
            isUser = true
        )
        messageCache.add(msg)
        return msg
    }

    fun addBotMessage(message: ChatMessage) {
        messageCache.add(message)
    }

    suspend fun generateBotReply(userText: String): ChatMessage {
        delay(400)
        val replyText = when {
            userText.contains("안녕") -> "안녕하세요 👋"
            userText.contains("이름") -> "저는 I:BOT이에요 🤖"
            userText.contains("도움") -> "무엇을 도와드릴까요?"
            else -> "“$userText”에 대해 생각해볼게요!"
        }
        val reply = ChatMessage(
            id = Random.nextLong(),
            text = replyText,
            isUser = false
        )
        messageCache.add(reply)
        return reply
    }

    suspend fun saveSnapshot(conversationId: Long?, messages: List<ChatMessage>) {
        delay(100)
        messageCache.clear()
        messageCache.addAll(messages)
    }
}