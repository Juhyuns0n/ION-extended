package com.ion.app.data.datasource.remote.voicereport

import com.ion.app.data.dto.response.voicereport.RecentVoiceSummaryDto
import com.ion.app.data.dto.response.voicereport.VoiceReportListResponseDto
import com.ion.app.data.dto.response.voicereport.VoiceReportResponseDto
import okhttp3.MultipartBody

interface VoiceReportRemoteDataSource {
    suspend fun uploadVoiceReport(audio: MultipartBody.Part): VoiceReportResponseDto
    suspend fun getVoiceReports(): VoiceReportListResponseDto
    suspend fun getVoiceReportById(id: Long): VoiceReportResponseDto
    suspend fun getRecentSummary(): RecentVoiceSummaryDto
}