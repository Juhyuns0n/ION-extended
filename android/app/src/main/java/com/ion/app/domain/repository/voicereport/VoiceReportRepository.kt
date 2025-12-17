package com.ion.app.domain.repository.voicereport

import com.ion.app.data.dto.response.voicereport.VoiceReportListItemDto
import com.ion.app.domain.model.voicereport.RecentVoiceSummaryModel
import com.ion.app.domain.model.voicereport.VoiceReportListItemModel
import com.ion.app.domain.model.voicereport.VoiceReportModel
import okhttp3.MultipartBody

interface VoiceReportRepository {
    suspend fun uploadVoiceReport(audioFile: MultipartBody.Part): Result<VoiceReportModel>
    suspend fun getVoiceReports(): Result<List<VoiceReportListItemModel>>
    suspend fun getVoiceReportById(id: Long): Result<VoiceReportModel>
    suspend fun getRecentSummary(): Result<RecentVoiceSummaryModel>
}