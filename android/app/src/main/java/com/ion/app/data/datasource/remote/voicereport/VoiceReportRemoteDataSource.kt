package com.ion.app.data.datasource.remote.voicereport

import com.ion.app.data.dto.response.voicereport.RecentVoiceSummaryDto
import com.ion.app.data.dto.response.voicereport.VoiceReportListResponseDto
import com.ion.app.data.dto.response.voicereport.VoiceReportResponseDto
import com.ion.app.data.dto.response.voicereport.VoiceReportJobStatusDto
import com.ion.app.data.dto.response.voicereport.VoiceReportSubmissionDto
import okhttp3.MultipartBody

interface VoiceReportRemoteDataSource {
    suspend fun submitVoiceReport(media: MultipartBody.Part): VoiceReportSubmissionDto
    suspend fun getVoiceReportStatus(id: Long): VoiceReportJobStatusDto
    suspend fun getVoiceReports(): VoiceReportListResponseDto
    suspend fun getVoiceReportById(id: Long): VoiceReportResponseDto
    suspend fun getRecentSummary(): RecentVoiceSummaryDto
}
