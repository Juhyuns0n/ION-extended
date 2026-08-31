package com.ion.app.data.service.voicereport

import com.ion.app.data.dto.response.voicereport.RecentVoiceSummaryDto
import retrofit2.http.Query
import com.ion.app.data.dto.response.voicereport.VoiceReportListResponseDto
import com.ion.app.data.dto.response.voicereport.VoiceReportResponseDto
import com.ion.app.data.dto.response.voicereport.VoiceReportJobStatusDto
import com.ion.app.data.dto.response.voicereport.VoiceReportSubmissionDto
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface VoiceReportService {
    @Multipart
    @POST("api/voice-reports")
    suspend fun submitVoiceReport(
        @Part video: MultipartBody.Part
    ): VoiceReportSubmissionDto

    @GET("/api/voice-reports/{id}/status")
    suspend fun getVoiceReportStatus(
        @Path("id") id: Long
    ): VoiceReportJobStatusDto

    @GET("/api/voice-reports/list")
    suspend fun getVoiceReports(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): VoiceReportListResponseDto

    @GET("/api/voice-reports/{id}")
    suspend fun getVoiceReportById(
        @Path("id") id: Long
    ): VoiceReportResponseDto

    @GET("/api/voice-reports/recentSummary")
    suspend fun getRecentSummary(
    ): RecentVoiceSummaryDto
}
