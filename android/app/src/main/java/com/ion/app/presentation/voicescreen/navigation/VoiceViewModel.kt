package com.ion.app.presentation.voicescreen.navigation

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ion.app.domain.repository.voicereport.VoiceReportRepository
import com.ion.app.domain.model.voicereport.VoiceReportJobStatus
import com.ion.app.presentation.voicescreen.VoiceUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class VoiceViewModel @Inject constructor(
    private val repository: VoiceReportRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = _uiState

    // 네비게이션 인자: reportId 읽기
    private val reportId: Long? = savedStateHandle["reportId"]
    private var uploadJob: Job? = null

    init {
        Log.d("VoiceViewModel", "SavedStateHandle reportId=$reportId")
        // 단일 보고서
        reportId?.let {
            Log.d("VoiceViewModel", "Loading single report $it")
            loadVoiceReport(it)
        } ?: run {
            // 리스트 (홈)
            loadVoiceReports()
        }
    }

    companion object {
        private const val NO_RECENT_SUMMARY_MESSAGE =
            "최근 대화가 없어요!\n동영상을 추가해보세요."
        internal const val STATUS_POLL_INTERVAL_MS = 2_000L
        internal const val MAX_STATUS_POLLS = 900
    }

    fun loadRecentSummary() {
        viewModelScope.launch {
            repository.getRecentSummary()
                .onSuccess { summary ->
                    _uiState.update { s ->
                        s.copy(
                            recentSummary = summary,
                            recentSummaryError = null,
                            errorMessage = null
                        )
                    }
                }
                .onFailure {
                    _uiState.update { s ->
                        s.copy(
                            recentSummary = null,
                            recentSummaryError = NO_RECENT_SUMMARY_MESSAGE
                        )
                    }
                }
        }
    }

    fun loadVoiceReports() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.getVoiceReports()
                .onSuccess { reports ->
                    _uiState.update {
                        it.copy(
                            reports = reports,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    Log.d("VoiceViewModel", "Reports loaded: ${reports.size}")
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.localizedMessage ?: "리포트를 불러오지 못했습니다."
                        )
                    }
                }
        }
    }

    fun loadVoiceReport(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.getVoiceReportById(id)
                .onSuccess { report ->
                    _uiState.update {
                        it.copy(
                            selectedReport = report,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { e ->
                    Log.e("VoiceViewModel", "Failed to load report $id", e)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.localizedMessage ?: "리포트를 불러오지 못했습니다."
                        )
                    }
                }
        }
    }

    fun uploadVoiceReport(file: MultipartBody.Part) {
        uploadJob?.cancel()
        uploadJob = viewModelScope.launch {
            // 업로드 시작
            _uiState.update {
                it.copy(
                    isUploading = true,
                    uploadMessage = null,
                    errorMessage = null
                )
            }

            val submission = repository.submitVoiceReport(file).getOrElse { error ->
                showUploadFailure(error.message ?: "업로드에 실패했습니다.")
                return@launch
            }

            var remainingPolls = MAX_STATUS_POLLS
            while (isActive && remainingPolls > 0) {
                remainingPolls -= 1
                val job = repository.getVoiceReportStatus(submission.reportId).getOrElse { error ->
                    showUploadFailure(error.message ?: "리포트 상태를 확인하지 못했습니다.")
                    return@launch
                }

                when (job.status) {
                    VoiceReportJobStatus.PENDING,
                    VoiceReportJobStatus.PROCESSING -> delay(STATUS_POLL_INTERVAL_MS)

                    VoiceReportJobStatus.COMPLETED -> {
                        val report = job.report
                        if (report == null) {
                            showUploadFailure("완료된 리포트 데이터가 없습니다.")
                            return@launch
                        }
                        _uiState.update {
                            it.copy(
                                isUploading = false,
                                uploadMessage = "분석이 완료됐어요!\n새 리포트가 아래 ‘분석 리포트’에 추가되었어요.",
                                selectedReport = report,
                                errorMessage = null
                            )
                        }
                        loadVoiceReports()
                        loadRecentSummary()
                        return@launch
                    }

                    VoiceReportJobStatus.FAILED -> {
                        showUploadFailure(job.errorMessage ?: "리포트 분석에 실패했습니다.")
                        return@launch
                    }
                }
            }

            if (isActive) {
                showUploadFailure("리포트 처리 시간이 초과되었습니다.")
            }
        }
    }

    private fun showUploadFailure(message: String) {
        _uiState.update {
            it.copy(
                isUploading = false,
                errorMessage = message,
                uploadMessage = "업로드에 실패했어요.\n네트워크 상태를 확인한 뒤 다시 시도해 주세요."
            )
        }
    }

    fun clearUploadMessage() {
        _uiState.update { it.copy(uploadMessage = null) }
    }
}
