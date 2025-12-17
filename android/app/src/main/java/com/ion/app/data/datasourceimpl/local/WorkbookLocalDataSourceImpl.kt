//package com.ion.app.data.datasourceimpl.local
//
//import android.util.Log
//import com.ion.app.data.datasource.local.workbook.WorkbookLocalDataSource
//import com.ion.app.data.dto.request.workbook.WorkbookAnswerRequestDto
//import com.ion.app.domain.model.workbook.WorkbookChapterModel
//import com.ion.app.domain.model.workbook.WorkbookFeedbackModel
//import com.ion.app.domain.model.workbook.WorkbookLessonModel
//import javax.inject.Inject
//
//class WorkbookLocalDataSourceImpl @Inject constructor() : WorkbookLocalDataSource {
//
//    // 하드코딩된 더미 데이터
//    private val dummyLessons = mutableListOf(
//        WorkbookLessonModel(
//            chapterId = 1,
//            workbookId = 101L,
//            workbookTitle = "감정 인식 대화",
//            activities = listOf(
//                WorkbookLessonModel.ActivityItem(
//                    type = "multiple_choice",
//                    instruction = "아이가 화났을 때 부모가 해야 할 반응으로 가장 적절한 것은?",
//                    options = listOf("화를 내며 지적한다", "감정을 인정해준다", "무시한다"),
//                    situation = "아이가 친구와 싸워 울고 있는 상황",
//                    yourResponse = "",
//                    optimalOption = "감정을 인정해준다",
//                    optimalWriting = null,
//                    optimalSim = null
//                ),
//                WorkbookLessonModel.ActivityItem(
//                    type = "short_answer",
//                    instruction = "아이의 감정을 공감하는 말을 직접 적어보세요.",
//                    options = emptyList(),
//                    situation = "아이가 친구와 싸워 속상해합니다.",
//                    yourResponse = "",
//                    optimalOption = null,
//                    optimalWriting = "많이 속상했겠다. 친구랑 싸워서 마음이 아프겠네.",
//                    optimalSim = null
//                )
//            )
//        ),
//        WorkbookLessonModel(
//            chapterId = 2,
//            workbookId = 202L,
//            workbookTitle = "긍정적 피드백 연습",
//            activities = listOf(
//                WorkbookLessonModel.ActivityItem(
//                    type = "multiple_choice",
//                    instruction = "아이의 노력을 칭찬하는 적절한 표현은?",
//                    options = listOf("결과가 이게 뭐야", "그래도 열심히 했구나!", "다음엔 더 잘해"),
//                    situation = "아이가 숙제는 다 못했지만 최선을 다한 상황",
//                    yourResponse = "",
//                    optimalOption = "그래도 열심히 했구나!",
//                    optimalWriting = null,
//                    optimalSim = null
//                )
//            )
//        )
//    )
//
//    private val dummyFeedbacks = mutableMapOf<Long, WorkbookFeedbackModel>(
//        101L to WorkbookFeedbackModel(
//            workbookFeedback = "감정을 인정하고 아이의 입장을 먼저 이해하려는 시도가 좋습니다."
//        ),
//        202L to WorkbookFeedbackModel(
//            workbookFeedback = "노력을 칭찬하며 자존감을 높여주는 방향이 적절합니다."
//        )
//    )
//
//    // 워크북 상세 불러오기
//    override suspend fun getWorkbookLesson(chapterId: Int, workbookId: Long): WorkbookLessonModel {
//        return dummyLessons.find { it.chapterId == chapterId && it.workbookId == workbookId }
//            ?: throw IllegalArgumentException("해당 워크북이 존재하지 않습니다: $workbookId")
//    }
//
//    // 워크북 정답 저장 (단순 로그 출력)
//    override suspend fun postWorkbookAnswer(
//        chapterId: Int,
//        workbookId: Long,
//        request: WorkbookAnswerRequestDto
//    ) {
//        Log.d("WorkbookLocalData", "postWorkbookAnswer() called for workbookId=$workbookId with $request")
//
//        val lesson = dummyLessons.find { it.workbookId == workbookId }
//        lesson?.let {
//            val updatedActivities = it.activities.map { activity ->
//                when {
//                    request.optionAnswer.isNotBlank() -> activity.copy(yourResponse = request.optionAnswer)
//                    request.writingAnswer.isNotBlank() -> activity.copy(yourResponse = request.writingAnswer)
//                    request.simAnswer.isNotBlank() -> activity.copy(yourResponse = request.simAnswer)
//                    else -> activity
//                }
//            }
//            val updatedLesson = it.copy(activities = updatedActivities)
//            dummyLessons[dummyLessons.indexOf(it)] = updatedLesson
//        }
//    }
//
//
//    // 워크북 피드백
//    override suspend fun getWorkbookFeedback(workbookId: Long): WorkbookFeedbackModel {
//        return dummyFeedbacks[workbookId]
//            ?: WorkbookFeedbackModel(workbookFeedback = "피드백 데이터가 없습니다.")
//    }
//
//    // 챕터별 워크북 목록
//    override suspend fun getWorkbookChapter(chapterId: Int): WorkbookChapterModel {
//        return dummyChapters.find { it.chapterId == chapterId }
//            ?: throw IllegalArgumentException("해당 챕터가 존재하지 않습니다: $chapterId")
//    }
//}