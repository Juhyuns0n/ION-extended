package com.ion.app.data.datasource.remote.workbook

import com.ion.app.data.dto.request.workbook.SimulationNextLineRequestDto
import com.ion.app.data.dto.request.workbook.WorkbookAnswerRequestDto
import com.ion.app.data.dto.response.workbook.SimulationFeedbackResponseDto
import com.ion.app.data.dto.response.workbook.SimulationLessonResponseDto
import com.ion.app.data.dto.response.workbook.SimulationNextLineResponseDto
import com.ion.app.data.dto.response.workbook.WorkbookChapterResponseDto
import com.ion.app.data.dto.response.workbook.WorkbookFeedbackResponseDto
import com.ion.app.data.dto.response.workbook.WorkbookLessonResponseDto
import com.ion.app.data.dto.response.workbook.WorkbookTheoryResponseDto

interface WorkbookRemoteDataSource {
    suspend fun createWorkbook(userId: Long): Result<Unit>

    suspend fun getWorkbookLesson(
        chapterId: Int,
        workbookId: Long
    ): WorkbookLessonResponseDto

    suspend fun postWorkbookAnswer(
        chapterId: Int,
        workbookId: Long,
        request: WorkbookAnswerRequestDto
    )

    suspend fun getWorkbookFeedback(
        chapterId: Int,
        lessonId: Long,
    ): WorkbookFeedbackResponseDto?

    suspend fun getSimulationLesson(
        chapterId: Int
    ): SimulationLessonResponseDto

    suspend fun getNextSimulationLine(
        chapterId: Int,
        request: SimulationNextLineRequestDto
    ): SimulationNextLineResponseDto

    suspend fun getSimulationFeedback(chapterId: Int): SimulationFeedbackResponseDto?

    suspend fun getLessonList(
        chapterId: Int
    ): WorkbookChapterResponseDto

    suspend fun getChapterTheory(
        chapterId: Int
    ): WorkbookTheoryResponseDto
}

