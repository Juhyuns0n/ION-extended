package com.ion.app.data.repositoryimpl.workbook

import com.ion.app.data.datasource.remote.workbook.WorkbookRemoteDataSource
import com.ion.app.data.dto.request.workbook.WorkbookAnswerRequestDto
import com.ion.app.data.mapper.workbook.toDomain
import com.ion.app.data.mapper.workbook.toNextLineRequestDto
import com.ion.app.domain.model.workbook.SimulationFeedbackModel
import com.ion.app.domain.model.workbook.SimulationLessonModel
import com.ion.app.domain.model.workbook.WorkbookChapterModel
import com.ion.app.domain.model.workbook.WorkbookFeedbackModel
import com.ion.app.domain.model.workbook.WorkbookLessonModel
import com.ion.app.domain.model.workbook.WorkbookTheoryModel
import com.ion.app.domain.repository.workbook.WorkbookRepository
import javax.inject.Inject

class WorkbookRepositoryImpl @Inject constructor(
    private val remoteDataSource: WorkbookRemoteDataSource
) : WorkbookRepository {

    override suspend fun createWorkbook(userId: Long): Result<Unit> {
        return remoteDataSource.createWorkbook(userId)
    }

    override suspend fun getWorkbookLesson(
        chapterId: Int,
        lessonId: Long
    ): WorkbookLessonModel =
        remoteDataSource
            .getWorkbookLesson(chapterId, lessonId)
            .toDomain(chapterId, lessonId)

    override suspend fun getSimulationLesson(chapterId: Int): SimulationLessonModel =
        remoteDataSource.getSimulationLesson(chapterId).toDomain(chapterId)

    override suspend fun getNextSimulationLine(
        chapterId: Int,
        dialogues: List<SimulationLessonModel.Dialogue>
    ): String {
        val request = dialogues.toNextLineRequestDto()
        val response = remoteDataSource.getNextSimulationLine(chapterId, request)
        return response.nextLine
    }

    override suspend fun getSimulationFeedback(
        chapterId: Int
    ): SimulationFeedbackModel? =
        remoteDataSource
            .getSimulationFeedback(chapterId)
            ?.toDomain()

    override suspend fun getWorkbookFeedback(
        chapterId: Int,
        lessonId: Long
    ): WorkbookFeedbackModel? {
        val dto = remoteDataSource.getWorkbookFeedback(chapterId, lessonId)
        return dto?.toDomain()
    }

    override suspend fun getLessonList(
        chapterId: Int
    ): WorkbookChapterModel =
        remoteDataSource.getLessonList(chapterId).toDomain(chapterId)

    override suspend fun getChapterTheory(
        chapterId: Int
    ): WorkbookTheoryModel =
        remoteDataSource.getChapterTheory(chapterId).toDomain(chapterId)

    override suspend fun postWorkbookAnswer(
        chapterId: Int,
        lessonId: Long,
        request: WorkbookAnswerRequestDto
    ) = remoteDataSource.postWorkbookAnswer(chapterId, lessonId, request)
}
