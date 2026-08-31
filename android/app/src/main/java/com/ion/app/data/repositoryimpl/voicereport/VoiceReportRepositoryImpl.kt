package com.ion.app.data.repositoryimpl.voicereport

import android.util.Log
import com.ion.app.data.datasource.remote.voicereport.VoiceReportRemoteDataSource
import com.ion.app.data.mapper.voicereport.toDomainList
import com.ion.app.data.mapper.voicereport.toDomain
import com.ion.app.domain.model.voicereport.RecentVoiceSummaryModel
import com.ion.app.domain.model.voicereport.VoiceReportListItemModel
import com.ion.app.domain.model.voicereport.VoiceReportJobModel
import com.ion.app.domain.model.voicereport.VoiceReportJobStatus
import com.ion.app.domain.model.voicereport.VoiceReportModel
import com.ion.app.domain.model.voicereport.VoiceReportSubmissionModel
import com.ion.app.domain.repository.voicereport.VoiceReportRepository
import okhttp3.MultipartBody
import javax.inject.Inject

class VoiceReportRepositoryImpl @Inject constructor(
    private val remoteDataSource: VoiceReportRemoteDataSource
) : VoiceReportRepository {

    override suspend fun submitVoiceReport(mediaFile: MultipartBody.Part): Result<VoiceReportSubmissionModel> =
        runCatching {
            val response = remoteDataSource.submitVoiceReport(mediaFile)
            VoiceReportSubmissionModel(response.reportId, VoiceReportJobStatus.valueOf(response.status.name))
        }.onFailure { e ->
            Log.e("VoiceReportRepo", "Upload failed", e)
        }

    override suspend fun getVoiceReportStatus(id: Long): Result<VoiceReportJobModel> =
        runCatching {
            val response = remoteDataSource.getVoiceReportStatus(id)
            VoiceReportJobModel(
                reportId = response.reportId,
                status = VoiceReportJobStatus.valueOf(response.status.name),
                report = response.report?.toDomain(),
                errorMessage = response.errorMessage
            )
        }

    override suspend fun getVoiceReports(): Result<List<VoiceReportListItemModel>> =
        runCatching {
            val response = remoteDataSource.getVoiceReports()
            response.content.toDomainList()
        }


    override suspend fun getVoiceReportById(id: Long): Result<VoiceReportModel> =
        runCatching {
            val response = remoteDataSource.getVoiceReportById(id)
            Log.d("VoiceReportRepo", "Got response: $response")
            val domain = response.toDomain()
            Log.d("VoiceReportRepo", "After toDomain() -> $domain")
            domain
        }

    override suspend fun getRecentSummary(): Result<RecentVoiceSummaryModel> =
        runCatching {
            val dto = remoteDataSource.getRecentSummary()
            dto.toDomain()
        }
}
