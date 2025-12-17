package com.ion.app.data.dto.request.workbook

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorkbookAnswerRequestDto(
    @SerialName("firstDescriptiveFormAnswer") val firstDescriptiveFormAnswer: String,
    @SerialName("secondDescriptiveFormAnswer") val secondDescriptiveFormAnswer: String,
    @SerialName("firstSelectiveAnswer") val firstSelectiveAnswer: String,
    @SerialName("secondSelectiveAnswer") val secondSelectiveAnswer: String,
    @SerialName("thirdSelectiveAnswer") val thirdSelectiveAnswer: String
)
