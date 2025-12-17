package com.ion.app.data.datasourceimpl.remote.voicereport

import com.ion.app.data.datasource.remote.voicereport.VoiceReportRemoteDataSource
import com.ion.app.data.dto.response.voicereport.RecentVoiceSummaryDto
import com.ion.app.data.dto.response.voicereport.VoiceReportListResponseDto
import com.ion.app.data.dto.response.voicereport.VoiceReportResponseDto
import com.ion.app.data.service.voicereport.VoiceReportService
import okhttp3.MultipartBody
import javax.inject.Inject

class VoiceReportRemoteDataSourceImpl @Inject constructor(
    private val service: VoiceReportService
) : VoiceReportRemoteDataSource {

    override suspend fun uploadVoiceReport(audio: MultipartBody.Part): VoiceReportResponseDto {
        return service.uploadVoiceReport(audio)
    }

    override suspend fun getVoiceReports(): VoiceReportListResponseDto {
        return service.getVoiceReports()
    }

    override suspend fun getVoiceReportById(id: Long): VoiceReportResponseDto {
        return service.getVoiceReportById(id)
    }

    override suspend fun getRecentSummary(): RecentVoiceSummaryDto {
        return service.getRecentSummary()
    }

}