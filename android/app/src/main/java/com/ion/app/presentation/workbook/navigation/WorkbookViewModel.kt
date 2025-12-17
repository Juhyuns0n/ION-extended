package com.ion.app.presentation.workbook.navigation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ion.app.data.mapper.workbook.toAnswerRequestDto
import com.ion.app.domain.model.workbook.SimulationLessonModel
import com.ion.app.domain.model.workbook.WorkbookLessonModel
import com.ion.app.domain.repository.workbook.WorkbookRepository
import com.ion.app.presentation.workbook.WorkbookUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class WorkbookViewModel @Inject constructor(
    private val repository: WorkbookRepository
) : ViewModel() {

    companion object {
        // 시뮬레이션 왕복(부모 입력 + AI 응답) 몇 번 후에 피드백 호출할지
        private const val MAX_SIMULATION_TURNS = 10
    }

    private val _uiState = MutableStateFlow(WorkbookUiState())
    val uiState: StateFlow<WorkbookUiState> = _uiState

    private val _postResult = MutableStateFlow(false)

    fun updateChapterInfo(chapterId: Int, chapterName: String) {
        _uiState.update {
            it.copy(
                chapterId = chapterId,
                chapterName = chapterName
            )
        }
        loadLessonList(chapterId)
    }


    fun loadChapterTheory(chapterId: Int) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val theory = repository.getChapterTheory(chapterId)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        chapterId = chapterId,
                        chapterName = theory.chapterTitle,       // 챕터명 재사용
                        theoryNecessity = theory.necessity,
                        theoryStudyGoal = theory.studyGoal,
                        theoryNotion = theory.notion
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    private fun loadLessonList(chapterId: Int) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                // /api/workbooks/{chapterId} 결과가 매핑된 도메인 모델
                val chapter = repository.getLessonList(chapterId)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        chapterId = chapterId,
                        chapterName = chapter.chapterTitle,     // "자기 인식"
                        chapterProgress = chapter.chapterProgress, // 0 ~ 100 or 서버 값
                        lessonList = chapter.lessons            // List<LessonItem>
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }


    fun loadLesson(chapterId: Int, lessonId: Long) {
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                isSimulation = false,
                workbookFeedback = null,
                simulationFeedback = null,
                isFeedbackLoading = false
            )
        }

        viewModelScope.launch {
            try {
                if (lessonId == 5L) {
                    // 시뮬레이션 레슨
                    val sim = repository.getSimulationLesson(chapterId)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            chapterId = chapterId,
                            lessonId = lessonId,
                            lessonName = sim.lessonTitle,
                            isSimulation = true,
                            simulationSituationExplain = sim.situationExplain,
                            simulationDialogues = sim.dialogues,
                            simulationTurnCount = 0,
                            isSimulationSending = false,
                            isSimulationFinished = false,
                            activityList = emptyList()
                        )
                    }
                } else {
                    // 일반 워크북 레슨
                    val lesson = repository.getWorkbookLesson(chapterId, lessonId)

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            chapterId = chapterId,
                            lessonId = lesson.workbookId,
                            lessonName = lesson.workbookTitle,
                            activityList = lesson.activities,
                            isSimulation = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    fun selectOption(pageIndex: Int, selectedIndex: Int) {
        _uiState.update { state ->
            val newList = state.activityList.toMutableList()
            val target = newList[pageIndex]
            newList[pageIndex] = target.copy(
                yourResponse = selectedIndex.toString()
            )
            state.copy(activityList = newList)
        }
    }


    fun enterTextAnswer(index: Int, newText: String) {
        val updatedList = _uiState.value.activityList.toMutableList()
        val updatedItem = updatedList[index].copy(yourResponse = newText)
        updatedList[index] = updatedItem
        _uiState.value = _uiState.value.copy(activityList = updatedList)
    }

    fun postWorkbookAnswer() {
        val current = _uiState.value
        val realChapterId = current.chapterId ?: return
        val realLessonId = current.lessonId ?: return

        // 이미 피드백 로딩 중이면 또 보내지 않기
        if (current.isFeedbackLoading) return

        val lessonModel = WorkbookLessonModel(
            chapterId = realChapterId,
            workbookId = realLessonId,
            workbookTitle = current.lessonName.orEmpty(),
            activities = current.activityList
        )
        val dto = lessonModel.toAnswerRequestDto()

        // 피드백 기다리기
        _uiState.update { it.copy(isFeedbackLoading = true) }

        viewModelScope.launch {
            try {
                repository.postWorkbookAnswer(realChapterId, realLessonId, dto)
                _postResult.value = true

                // 바로 피드백 요청
                loadWorkbookFeedback(realChapterId, realLessonId, showError = true)

            } catch (e: kotlinx.coroutines.CancellationException) {
                println("DEBUG: Post cancelled")
                _uiState.update { it.copy(isFeedbackLoading = false) }
            } catch (e: java.io.IOException) {
                println("DEBUG: Network error -> ${e.message}")
                _uiState.update { it.copy(isFeedbackLoading = false) }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isFeedbackLoading = false) }
            }
        }
    }

    fun loadWorkbookFeedback(
        chapterId: Int,
        lessonId: Long,
        showError: Boolean = true
    ) {
        _uiState.update {
            it.copy(
                isFeedbackLoading = true,
                // silent 모드일 때는 기존 errorMessage 유지
                errorMessage = if (showError) null else it.errorMessage
            )
        }

        viewModelScope.launch {
            try {
                val feedback = repository.getWorkbookFeedback(chapterId, lessonId)
                _uiState.update {
                    it.copy(
                        isFeedbackLoading = false,
                        workbookFeedback = feedback?.workbookFeedback
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isFeedbackLoading = false,
                        errorMessage = if (showError) {
                            e.message ?: "Unknown error"
                        } else {
                            it.errorMessage   //무시
                        }
                    )
                }
            }
        }
    }

    fun sendSimulationLine(userLine: String) {
        val current = _uiState.value
        val chapterId = current.chapterId ?: return
        if (!current.isSimulation) return
        if (current.isSimulationFinished) return
        if (current.simulationDialogues.isEmpty()) return

        // 1) 마지막 대화에 userLine 채워 넣기
        val filledDialogues = current.simulationDialogues.toMutableList()
        val lastIdx = filledDialogues.lastIndex
        val last = filledDialogues[lastIdx]

        if (last.userLine == null) {
            filledDialogues[lastIdx] = last.copy(userLine = userLine)
        } else {
            // 혹시 이미 채워져 있으면 새 턴으로 추가
            filledDialogues.add(
                SimulationLessonModel.Dialogue(
                    userLine = userLine,
                    aiLine = null
                )
            )
        }

        // UI에 바로 반영 (방금 쓴 말)
        _uiState.update {
            it.copy(
                simulationDialogues = filledDialogues,
                isSimulationSending = true,
                errorMessage = null
            )
        }

        // 2) 서버에 다음 대사 요청
        viewModelScope.launch {
            try {
                val nextLine = repository.getNextSimulationLine(chapterId, filledDialogues)

                var needFeedback = false

                _uiState.update { state ->
                    val list = state.simulationDialogues.toMutableList()
                    list.add(
                        SimulationLessonModel.Dialogue(
                            userLine = null,
                            aiLine = nextLine
                        )
                    )
                    val newTurnCount = state.simulationTurnCount + 1
                    needFeedback = newTurnCount >= MAX_SIMULATION_TURNS

                    state.copy(
                        simulationDialogues = list,
                        simulationTurnCount = newTurnCount,
                        isSimulationSending = false,
                        isSimulationFinished = if (needFeedback) true else state.isSimulationFinished
                    )
                }

                // 3) 정해진 턴 수가 되면 피드백 요청
                if (needFeedback) {
                    loadSimulationFeedback(chapterId)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSimulationSending = false,
                        errorMessage = e.message ?: "시뮬레이션 대화를 불러오지 못했어요."
                    )
                }
            }
        }
    }

    fun loadSimulationFeedback(chapterId: Int) {
        _uiState.update { it.copy(isFeedbackLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val feedback = repository.getSimulationFeedback(chapterId)
                _uiState.update {
                    it.copy(
                        isFeedbackLoading = false,
                        simulationFeedback = feedback?.simulationFeedback,
                        isSimulationFinished = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isFeedbackLoading = false,
                        errorMessage = e.message ?: "시뮬레이션 피드백을 불러오지 못했어요."
                    )
                }
            }
        }
    }

}