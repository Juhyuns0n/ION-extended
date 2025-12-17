package com.ion.app.data.dto.response.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class PropensityTestResponseDto(
    @SerialName("propensityTestId") val propensityTestId: Int,
    @SerialName("propensityTestQuestion") val propensityTestQuestion: String
)