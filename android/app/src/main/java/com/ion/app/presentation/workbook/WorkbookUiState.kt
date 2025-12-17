package com.ion.app.presentation.workbook

import com.ion.app.domain.model.workbook.SimulationLessonModel
import com.ion.app.domain.model.workbook.WorkbookChapterModel
import com.ion.app.domain.model.workbook.WorkbookLessonModel


data class WorkbookUiState(
    val chapterId: Int? = null,
    val chapterName: String? = null,
    val chapterProgress: Int = 0,

    val lessonName: String? = null,
    val lessonId: Long? = null,

    val lessonList: List<WorkbookChapterModel.LessonSummary> = emptyList(),

    val theoryNecessity: String? = null,
    val theoryStudyGoal: String? = null,
    val theoryNotion: String? = null,

    val workbookFeedback: String? = null,
    val isFeedbackLoading: Boolean = false,

    val isSimulation: Boolean = false,
    val simulationSituationExplain: String? = null,
    val simulationDialogues: List<SimulationLessonModel.Dialogue> = emptyList(),
    val simulationTurnCount: Int = 0,          // 몇 턴 왔다갔다 했는지
    val isSimulationSending: Boolean = false,  // 다음 라인 요청 중인지
    val isSimulationFinished: Boolean = false, // 피드백까지 끝났는지
    val simulationFeedback: String? = null,

    val activityList: List<WorkbookLessonModel.ActivityItem> = emptyList(),

    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedOption: Int = -1,
    val textAnswers: List<String> = emptyList()
)

val WorkbookUiState.hasFeedback: Boolean
    get() = (!isSimulation && !workbookFeedback.isNullOrBlank()) ||
            (isSimulation && !simulationFeedback.isNullOrBlank())

val WorkbookUiState.feedbackText: String
    get() = if (isSimulation) {
        simulationFeedback.orEmpty()
    } else {
        workbookFeedback.orEmpty()
    }


