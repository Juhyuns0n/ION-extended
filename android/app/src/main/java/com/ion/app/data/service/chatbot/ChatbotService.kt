package com.ion.app.data.service.chatbot

import com.ion.app.data.dto.request.chatbot.QuestionRequestDto
import com.ion.app.data.dto.response.chatbot.AnswerResponseDto
import com.ion.app.data.dto.response.chatbot.ChatbotListDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ChatbotService {

    @POST("api/chatbot")
    suspend fun ask(
        @Body body: QuestionRequestDto
    ): Response<AnswerResponseDto>

    @POST("api/chatbot/{chatSessionId}/close")
    suspend fun close(
        @Path("chatSessionId") chatSessionId: Long
    ): Response<Unit>

    @GET("api/chatbot/list")
    suspend fun getChatList(): Response<List<ChatbotListDto>>
}