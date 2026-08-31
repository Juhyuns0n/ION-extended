package com.ion.app.domain.model.voicereport

enum class VoiceReportJobStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED
}

data class VoiceReportSubmissionModel(
    val reportId: Long,
    val status: VoiceReportJobStatus
)

data class VoiceReportJobModel(
    val reportId: Long,
    val status: VoiceReportJobStatus,
    val report: VoiceReportModel? = null,
    val errorMessage: String? = null
)
