package com.ion.app.data.dto.response.workbook

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorkbookFeedbackResponseDto(
    @SerialName("workbookFeedback") val workbookFeedback: String
)