package com.ion.app.domain.repository.workbook

import com.ion.app.data.dto.request.workbook.WorkbookAnswerRequestDto
import com.ion.app.domain.model.workbook.SimulationFeedbackModel
import com.ion.app.domain.model.workbook.SimulationLessonModel
import com.ion.app.domain.model.workbook.WorkbookChapterModel
import com.ion.app.domain.model.workbook.WorkbookFeedbackModel
import com.ion.app.domain.model.workbook.WorkbookLessonModel
import com.ion.app.domain.model.workbook.WorkbookTheoryModel

interface WorkbookRepository {
    suspend fun createWorkbook(userId: Long): Result<Unit>
    suspend fun getWorkbookLesson(chapterId: Int, lessonId: Long): WorkbookLessonModel
    suspend fun getWorkbookFeedback(chapterId: Int, lessonId: Long): WorkbookFeedbackModel?

    suspend fun getSimulationLesson(chapterId: Int): SimulationLessonModel

    suspend fun getNextSimulationLine(chapterId: Int, dialogues: List<SimulationLessonModel.Dialogue>): String
    suspend fun getSimulationFeedback(chapterId: Int): SimulationFeedbackModel?

    suspend fun getLessonList(chapterId: Int): WorkbookChapterModel

    suspend fun getChapterTheory(chapterId: Int): WorkbookTheoryModel

    suspend fun postWorkbookAnswer(chapterId: Int, lessonId: Long, request: WorkbookAnswerRequestDto)
}