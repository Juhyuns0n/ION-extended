package com.ion.app.domain.repository.voicereport

import com.ion.app.domain.model.voicereport.RecentVoiceSummaryModel
import com.ion.app.domain.model.voicereport.VoiceReportJobModel
import com.ion.app.domain.model.voicereport.VoiceReportListItemModel
import com.ion.app.domain.model.voicereport.VoiceReportModel
import com.ion.app.domain.model.voicereport.VoiceReportSubmissionModel
import okhttp3.MultipartBody

interface VoiceReportRepository {
    suspend fun submitVoiceReport(mediaFile: MultipartBody.Part): Result<VoiceReportSubmissionModel>
    suspend fun getVoiceReportStatus(id: Long): Result<VoiceReportJobModel>
    suspend fun getVoiceReports(): Result<List<VoiceReportListItemModel>>
    suspend fun getVoiceReportById(id: Long): Result<VoiceReportModel>
    suspend fun getRecentSummary(): Result<RecentVoiceSummaryModel>
}
