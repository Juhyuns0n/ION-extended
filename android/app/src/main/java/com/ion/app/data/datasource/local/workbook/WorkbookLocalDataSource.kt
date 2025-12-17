package com.ion.app.data.datasource.local.workbook

import com.ion.app.data.dto.request.workbook.WorkbookAnswerRequestDto
import com.ion.app.domain.model.workbook.WorkbookChapterModel
import com.ion.app.domain.model.workbook.WorkbookFeedbackModel
import com.ion.app.domain.model.workbook.WorkbookLessonModel

interface WorkbookLocalDataSource {
    suspend fun getWorkbookLesson(chapterId: Int, workbookId: Long): WorkbookLessonModel
    suspend fun postWorkbookAnswer(chapterId: Int, workbookId: Long, request: WorkbookAnswerRequestDto)
    suspend fun getWorkbookFeedback(workbookId: Long): WorkbookFeedbackModel
    suspend fun getWorkbookChapter(chapterId: Int): WorkbookChapterModel
}
