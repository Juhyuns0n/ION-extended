package com.ion.app.data.service.workbook

import com.ion.app.data.dto.request.workbook.SimulationNextLineRequestDto
import com.ion.app.data.dto.request.workbook.WorkbookAnswerRequestDto
import com.ion.app.data.dto.response.workbook.SimulationFeedbackResponseDto
import com.ion.app.data.dto.response.workbook.SimulationLessonResponseDto
import com.ion.app.data.dto.response.workbook.SimulationNextLineResponseDto
import com.ion.app.data.dto.response.workbook.WorkbookChapterResponseDto
import com.ion.app.data.dto.response.workbook.WorkbookFeedbackResponseDto
import com.ion.app.data.dto.response.workbook.WorkbookLessonResponseDto
import com.ion.app.data.dto.response.workbook.WorkbookTheoryResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface WorkbookService {
    @POST("/api/workbooks/{userId}")
    suspend fun createWorkbook(
        @Path("userId") userId: Int
    ): WorkbookLessonResponseDto

    @GET("/api/workbooks/{chapterId}/{workbookId}")
    suspend fun getWorkbookLesson(
        @Path("chapterId") chapterId: Int,
        @Path("workbookId") workbookId: Long
    ): WorkbookLessonResponseDto

    @POST("/api/workbooks/{chapterId}/{workbookId}")
    suspend fun postWorkbookAnswer(
        @Path("chapterId") chapterId: Int,
        @Path("workbookId") workbookId: Long,
        @Body request: WorkbookAnswerRequestDto
    ): Unit

    @GET("/api/workbooks/{chapterId}/theory")
    suspend fun getWorkbookTheory(
        @Path("chapterId") chapterId: Int
    ): WorkbookTheoryResponseDto

    @GET("/api/workbooks/{chapterId}/{lessonId}/feedback")
    suspend fun getWorkbookFeedback(
        @Path("chapterId") chapterId: Int,
        @Path("lessonId") lessonId: Long
    ): Response<WorkbookFeedbackResponseDto>

    @GET("/api/workbooks/{chapterId}")
    suspend fun getWorkbookList(
        @Path("chapterId") chapterId: Int
    ): WorkbookChapterResponseDto

    @GET("/api/workbooks/{chapterId}/simulation")
    suspend fun getSimulationLesson(
        @Path("chapterId") chapterId: Int
    ): SimulationLessonResponseDto

    @GET("/api/workbooks/{chapterId}/simulationFeedback")
    suspend fun getSimulationFeedback(
        @Path("chapterId") chapterId: Int
    ): Response<SimulationFeedbackResponseDto>

    @POST("/api/workbooks/{chapterId}/getLine")
    suspend fun getNextSimulationLine(
        @Path("chapterId") chapterId: Int,
        @Body request: SimulationNextLineRequestDto
    ): SimulationNextLineResponseDto
}