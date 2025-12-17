package com.ion.app.data.datasourceimpl.remote.workbook

import com.ion.app.data.datasource.remote.workbook.WorkbookRemoteDataSource
import com.ion.app.data.dto.request.workbook.SimulationNextLineRequestDto
import com.ion.app.data.dto.request.workbook.WorkbookAnswerRequestDto
import com.ion.app.data.dto.response.workbook.SimulationFeedbackResponseDto
import com.ion.app.data.dto.response.workbook.SimulationLessonResponseDto
import com.ion.app.data.dto.response.workbook.SimulationNextLineResponseDto
import com.ion.app.data.dto.response.workbook.WorkbookChapterResponseDto
import com.ion.app.data.dto.response.workbook.WorkbookFeedbackResponseDto
import com.ion.app.data.dto.response.workbook.WorkbookLessonResponseDto
import com.ion.app.data.dto.response.workbook.WorkbookTheoryResponseDto
import com.ion.app.data.service.workbook.WorkbookService
import javax.inject.Inject

class WorkbookRemoteDataSourceImpl @Inject constructor(
    private val workbookService: WorkbookService
) : WorkbookRemoteDataSource {

    override suspend fun createWorkbook(userId: Long): Result<Unit> =
        runCatching {
            workbookService.createWorkbook(userId.toInt())
            Unit
        }

    override suspend fun getWorkbookLesson(
        chapterId: Int,
        lessonId: Long
    ): WorkbookLessonResponseDto =
        workbookService.getWorkbookLesson(chapterId, lessonId)

    override suspend fun postWorkbookAnswer(
        chapterId: Int,
        lessonId: Long,
        request: WorkbookAnswerRequestDto
    ) = workbookService.postWorkbookAnswer(chapterId, lessonId, request)

    override suspend fun getWorkbookFeedback(
        chapterId: Int,
        lessonId: Long
    ): WorkbookFeedbackResponseDto? {
        val response = workbookService.getWorkbookFeedback(chapterId, lessonId)
        // HTTP 실패 → null
        if (!response.isSuccessful) return null
        val body = response.body()
        // body 가 없는 경우 (Content-Length: 0)
        val raw = response.raw()
        if (raw.body?.contentLength() == 0L) {
            return null
        }
        // body 자체 null
        if (body == null) return null
        return body
    }

    override suspend fun getSimulationLesson(
        chapterId: Int
    ): SimulationLessonResponseDto =
        workbookService.getSimulationLesson(chapterId)

    override suspend fun getNextSimulationLine(
        chapterId: Int,
        request: SimulationNextLineRequestDto
    ): SimulationNextLineResponseDto =
        workbookService.getNextSimulationLine(chapterId, request)

    override suspend fun getSimulationFeedback(
        chapterId: Int
    ): SimulationFeedbackResponseDto? {
        val response = workbookService.getSimulationFeedback(chapterId)
        if (!response.isSuccessful) return null
        val raw = response.raw()
        if (raw.body?.contentLength() == 0L) {
            return null
        }
        return response.body()
    }

    override suspend fun getLessonList(chapterId: Int): WorkbookChapterResponseDto =
        workbookService.getWorkbookList(chapterId)

    override suspend fun getChapterTheory(chapterId: Int): WorkbookTheoryResponseDto =
        workbookService.getWorkbookTheory(chapterId)
}
