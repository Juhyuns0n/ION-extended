package com.ion.app.data.dto.response.workbook

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorkbookTheoryResponseDto(
    @SerialName("chapterTitle") val chapterTitle: String,
    @SerialName("necessity") val necessity: String,
    @SerialName("studyGoal") val studyGoal: String,
    @SerialName("notion") val notion: String
)