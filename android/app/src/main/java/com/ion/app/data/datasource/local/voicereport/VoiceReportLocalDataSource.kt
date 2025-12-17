package com.ion.app.data.datasource.local.voicereport
import com.ion.app.domain.model.voicereport.VoiceReportModel
import okhttp3.MultipartBody

interface VoiceReportLocalDataSource {
    suspend fun uploadVoiceReport(audioFile: MultipartBody.Part): VoiceReportModel

    suspend fun getVoiceReports(): List<VoiceReportModel>
    suspend fun getVoiceReportById(id: Long): VoiceReportModel?
}
