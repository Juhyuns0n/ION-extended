package com.ion.app.data.mapper.workbook

import com.ion.app.data.dto.request.workbook.SimulationNextLineRequestDto
import com.ion.app.data.dto.request.workbook.WorkbookAnswerRequestDto
import com.ion.app.data.dto.response.workbook.SimulationFeedbackResponseDto
import com.ion.app.data.dto.response.workbook.SimulationLessonResponseDto
import com.ion.app.data.dto.response.workbook.WorkbookChapterResponseDto
import com.ion.app.data.dto.response.workbook.WorkbookFeedbackResponseDto
import com.ion.app.data.dto.response.workbook.WorkbookLessonResponseDto
import com.ion.app.data.dto.response.workbook.WorkbookTheoryResponseDto
import com.ion.app.domain.model.workbook.SimulationFeedbackModel
import com.ion.app.domain.model.workbook.SimulationLessonModel
import com.ion.app.domain.model.workbook.WorkbookChapterModel
import com.ion.app.domain.model.workbook.WorkbookFeedbackModel
import com.ion.app.domain.model.workbook.WorkbookLessonModel
import com.ion.app.domain.model.workbook.WorkbookTheoryModel

// 서버에서 내려오는 Lesson → 도메인 모델
fun WorkbookLessonResponseDto.toDomain(
    chapterIdFromPath: Int,
    lessonIdFromPath: Long
): WorkbookLessonModel =
    WorkbookLessonModel(
        chapterId = chapterIdFromPath,
        workbookId = lessonIdFromPath,
        workbookTitle = lessonTitle,
        activities = listOf(
            // 1) 서술형 1
            WorkbookLessonModel.ActivityItem(
                type = "write",
                instruction = firstDescriptiveFormQuestion,
                options = emptyList(),
                situation = null,
                yourResponse = firstDescriptiveFormAnswer,
                optimalOption = null,
                optimalWriting = firstDescriptiveFormExample,
                optimalSim = null
            ),
            // 2) 서술형 2
            WorkbookLessonModel.ActivityItem(
                type = "write",
                instruction = secondDescriptiveFormQuestion,
                options = emptyList(),
                situation = null,
                yourResponse = secondDescriptiveFormAnswer,
                optimalOption = null,
                optimalWriting = secondDescriptiveFormExample,
                optimalSim = null
            ),
            // 3) 선택형 1
            WorkbookLessonModel.ActivityItem(
                type = "select",
                instruction = firstSelectiveQuestion,
                options = firstSelectiveOptions,
                situation = null,
                yourResponse = firstSelectiveAnswer,
                optimalOption = firstSelectiveExample,
                optimalWriting = null,
                optimalSim = null
            ),
            // 4) 선택형 2
            WorkbookLessonModel.ActivityItem(
                type = "select",
                instruction = secondSelectiveQuestion,
                options = secondSelectiveOptions,
                situation = null,
                yourResponse = secondSelectiveAnswer,
                optimalOption = secondSelectiveExample,
                optimalWriting = null,
                optimalSim = null
            ),
            // 5) 선택형 3
            WorkbookLessonModel.ActivityItem(
                type = "select",
                instruction = thirdSelectiveQuestion,
                options = thirdSelectiveOptions,
                situation = null,
                yourResponse = thirdSelectiveAnswer,
                optimalOption = thirdSelectiveExample,
                optimalWriting = null,
                optimalSim = null
            )
        )
    )

// 피드백 → 도메인
fun WorkbookFeedbackResponseDto.toDomain(): WorkbookFeedbackModel =
    WorkbookFeedbackModel(
        workbookFeedback = workbookFeedback
    )

// 챕터 리스트 → 도메인
fun WorkbookChapterResponseDto.toDomain(chapterId: Int): WorkbookChapterModel =
    WorkbookChapterModel(
        chapterId = chapterId,
        chapterTitle = chapterTitle,
        chapterProgress = chapterProgress,
        totalPages = totalPages,
        size = size,
        lessons = lessons.map { item ->
            WorkbookChapterModel.LessonSummary(
                lessonId = item.lessonId,
                lessonTitle = item.lessonTitle,
                progressStatus = item.progressStatus
            )
        }
    )

// 도메인 Lesson → 답안 요청 DTO (서술형 2개 + 선택형 3개)
fun WorkbookLessonModel.toAnswerRequestDto(): WorkbookAnswerRequestDto {
    val writeItems = activities.filter { it.type == "write" }
    val selectItems = activities.filter { it.type == "select" }

    val firstDescriptiveAnswer = writeItems.getOrNull(0)?.yourResponse.orEmpty()
    val secondDescriptiveAnswer = writeItems.getOrNull(1)?.yourResponse.orEmpty()

    val firstSelectiveAnswer = selectItems.getOrNull(0)?.yourResponse.orEmpty()
    val secondSelectiveAnswer = selectItems.getOrNull(1)?.yourResponse.orEmpty()
    val thirdSelectiveAnswer = selectItems.getOrNull(2)?.yourResponse.orEmpty()

    return WorkbookAnswerRequestDto(
        firstDescriptiveFormAnswer = firstDescriptiveAnswer,
        secondDescriptiveFormAnswer = secondDescriptiveAnswer,
        firstSelectiveAnswer = firstSelectiveAnswer,
        secondSelectiveAnswer = secondSelectiveAnswer,
        thirdSelectiveAnswer = thirdSelectiveAnswer
    )
}

// 시뮬레이션 레슨 → 도메인
fun SimulationLessonResponseDto.toDomain(
    chapterIdFromPath: Int
): SimulationLessonModel =
    SimulationLessonModel(
        chapterId = chapterIdFromPath,
        lessonTitle = lessonTitle,
        situationExplain = simulationSituationExplain,
        dialogues = dialogues.map {
            SimulationLessonModel.Dialogue(
                userLine = it.userLine,
                aiLine = it.aiLine
            )
        }
    )

// 시뮬레이션 피드백 → 도메인
fun SimulationFeedbackResponseDto.toDomain(): SimulationFeedbackModel =
    SimulationFeedbackModel(
        simulationFeedback = simulationFeedback
    )

// 이론(단원 설명) → 도메인
fun WorkbookTheoryResponseDto.toDomain(chapterId: Int): WorkbookTheoryModel =
    WorkbookTheoryModel(
        chapterId = chapterId,
        chapterTitle = chapterTitle,
        necessity = necessity,
        studyGoal = studyGoal,
        notion = notion
    )

// 시뮬레이션 대화 리스트 → 다음 대사 요청 DTO
fun List<SimulationLessonModel.Dialogue>.toNextLineRequestDto()
        : SimulationNextLineRequestDto =
    SimulationNextLineRequestDto(
        dialogues = this.map { d ->
            SimulationNextLineRequestDto.DialogueDto(
                userLine = d.userLine,
                aiLine = d.aiLine
            )
        }
    )
