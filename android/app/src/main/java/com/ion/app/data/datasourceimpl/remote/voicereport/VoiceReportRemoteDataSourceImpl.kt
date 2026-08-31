package com.ion.app.data.datasourceimpl.remote.voicereport

import com.ion.app.data.datasource.remote.voicereport.VoiceReportRemoteDataSource
import com.ion.app.data.dto.response.voicereport.RecentVoiceSummaryDto
import com.ion.app.data.dto.response.voicereport.VoiceReportListResponseDto
import com.ion.app.data.dto.response.voicereport.VoiceReportResponseDto
import com.ion.app.data.dto.response.voicereport.VoiceReportJobStatusDto
import com.ion.app.data.dto.response.voicereport.VoiceReportSubmissionDto
import com.ion.app.data.service.voicereport.VoiceReportService
import okhttp3.MultipartBody
import javax.inject.Inject

class VoiceReportRemoteDataSourceImpl @Inject constructor(
    private val service: VoiceReportService
) : VoiceReportRemoteDataSource {

    override suspend fun submitVoiceReport(media: MultipartBody.Part): VoiceReportSubmissionDto {
        return service.submitVoiceReport(media)
    }

    override suspend fun getVoiceReportStatus(id: Long): VoiceReportJobStatusDto {
        return service.getVoiceReportStatus(id)
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
