package com.ion.app.presentation.voicescreen

import com.ion.app.domain.model.voicereport.RecentVoiceSummaryModel
import com.ion.app.domain.model.voicereport.VoiceReportListItemModel
import com.ion.app.domain.model.voicereport.VoiceReportModel

data class VoiceUiState(
    val reports: List<VoiceReportListItemModel> = emptyList(), // 리포트 목록
    val selectedReport: VoiceReportModel? = null, // 단일 리포트
    val recentSummary: RecentVoiceSummaryModel? = null,
    val recentSummaryError: String? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val uploadMessage: String? = null,
    val isUploading: Boolean = false
)
