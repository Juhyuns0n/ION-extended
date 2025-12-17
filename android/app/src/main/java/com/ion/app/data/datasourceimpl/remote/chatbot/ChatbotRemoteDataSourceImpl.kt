package com.ion.app.data.datasourceimpl.remote.chatbot

import com.ion.app.data.datasource.remote.chatbot.ChatbotRemoteDataSource
import com.ion.app.data.dto.request.chatbot.QuestionRequestDto
import com.ion.app.data.dto.response.chatbot.AnswerResponseDto
import com.ion.app.data.dto.response.chatbot.ChatErrorResponseDto
import com.ion.app.data.dto.response.chatbot.ChatbotListDto
import com.ion.app.data.service.chatbot.ChatbotService
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import javax.inject.Inject

class ChatbotRemoteDataSourceImpl @Inject constructor(
    private val service: ChatbotService
) : ChatbotRemoteDataSource {

    private var currentSessionId: Long? = null

    override suspend fun sendQuestion(question: String): Pair<Long, String> {
        val response = service.ask(
            QuestionRequestDto(question = question)
        )

        if (response.isSuccessful) {
            val body: AnswerResponseDto =
                response.body() ?: throw IllegalStateException("응답이 비어 있습니다.")

            currentSessionId = body.sessionId
            return body.sessionId to body.answer
        } else {
            val msg = parseError(response.errorBody())
            throw IllegalStateException(msg)
        }
    }

    override suspend fun getChatList(): List<ChatbotListDto> {
        val res = service.getChatList()
        if (res.isSuccessful) return res.body() ?: emptyList()
        else throw IllegalStateException(parseError(res.errorBody()))
    }

    override suspend fun closeSessionIfNeeded() {
        val sessionId = currentSessionId ?: return

        try {
            val response = service.close(sessionId)
            // 실패해도 내부적으로 세션 초기화는 진행
            // (성공/실패와 관계 없이 클라이언트 세션 종료)
        } finally {
            currentSessionId = null
        }
    }

    /**
     * ErrorBody를 kotlinx.serialization(Json)으로 파싱
     */
    private fun parseError(errorBody: ResponseBody?): String {
        if (errorBody == null) return "챗봇 요청에 실패했습니다."

        return try {
            val str = errorBody.string()
            val parsed = Json.decodeFromString<ChatErrorResponseDto>(str)
            parsed.message
        } catch (e: Exception) {
            "챗봇 요청에 실패했습니다."
        }
    }
}