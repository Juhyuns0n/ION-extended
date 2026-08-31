package com.ion.app.presentation.voicescreen.navigation

import androidx.lifecycle.SavedStateHandle
import com.ion.app.domain.model.voicereport.*
import com.ion.app.domain.repository.voicereport.VoiceReportRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VoiceViewModelPollingTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun pollingStopsWhenJobCompletes() = runTest(mainDispatcherRule.testDispatcher) {
        val report = report()
        val repository = FakeRepository(
            statuses = ArrayDeque(
                listOf(
                    VoiceReportJobModel(42, VoiceReportJobStatus.PROCESSING),
                    VoiceReportJobModel(42, VoiceReportJobStatus.COMPLETED, report)
                )
            )
        )
        val viewModel = VoiceViewModel(repository, SavedStateHandle())

        viewModel.uploadVoiceReport(mediaPart())
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isUploading)
        assertEquals(report, viewModel.uiState.value.selectedReport)
        assertEquals(2, repository.statusCalls)
    }

    @Test
    fun pollingStopsWhenJobFails() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeRepository(
            statuses = ArrayDeque(
                listOf(VoiceReportJobModel(42, VoiceReportJobStatus.FAILED, errorMessage = "Processing failed"))
            )
        )
        val viewModel = VoiceViewModel(repository, SavedStateHandle())

        viewModel.uploadVoiceReport(mediaPart())
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isUploading)
        assertEquals("Processing failed", viewModel.uiState.value.errorMessage)
        assertEquals(1, repository.statusCalls)
    }

    @Test
    fun pollingStopsAfterMaximumAttempts() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeRepository(
            statuses = ArrayDeque(listOf(VoiceReportJobModel(42, VoiceReportJobStatus.PROCESSING))),
            repeatLastStatus = true
        )
        val viewModel = VoiceViewModel(repository, SavedStateHandle())

        viewModel.uploadVoiceReport(mediaPart())
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isUploading)
        assertEquals("리포트 처리 시간이 초과되었습니다.", viewModel.uiState.value.errorMessage)
        assertEquals(VoiceViewModel.MAX_STATUS_POLLS, repository.statusCalls)
    }

    private fun mediaPart(): MultipartBody.Part = MultipartBody.Part.createFormData(
        "video",
        "input.mp4",
        byteArrayOf(1).toRequestBody("video/mp4".toMediaType())
    )

    private fun report() = VoiceReportModel(
        id = 42,
        subTitle = "Test",
        day = "2026-08-29",
        conversationSummary = "Summary",
        overallFeedback = "Feedback",
        expression = Expression("p", "k", "pc", "kc", "ef"),
        changeProposal = emptyList(),
        emotion = Emotion(emptyList(), "emotion"),
        kidAttitude = "attitude",
        frequency = Frequency(1, 1, "frequency"),
        strength = "strength",
        kidsNickname = "kid"
    )

    private class FakeRepository(
        private val statuses: ArrayDeque<VoiceReportJobModel>,
        private val repeatLastStatus: Boolean = false
    ) : VoiceReportRepository {
        var statusCalls = 0
        private val lastStatus = statuses.lastOrNull()

        override suspend fun submitVoiceReport(mediaFile: MultipartBody.Part) =
            Result.success(VoiceReportSubmissionModel(42, VoiceReportJobStatus.PENDING))

        override suspend fun getVoiceReportStatus(id: Long): Result<VoiceReportJobModel> {
            statusCalls += 1
            statuses.removeFirstOrNull()?.let { return Result.success(it) }
            if (repeatLastStatus && lastStatus != null) return Result.success(lastStatus)
            return Result.failure(AssertionError("No configured status response"))
        }

        override suspend fun getVoiceReports() = Result.success(emptyList<VoiceReportListItemModel>())

        override suspend fun getVoiceReportById(id: Long) = Result.failure<VoiceReportModel>(AssertionError())

        override suspend fun getRecentSummary() = Result.failure<RecentVoiceSummaryModel>(AssertionError())
    }
}
