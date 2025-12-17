package com.ion.app.data.dto.response.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@kotlinx.serialization.Serializable
data class MembershipResultResponseDto(
    @SerialName("listScores")
    val listScores: List<Map<String, Int>>,
    @SerialName("userType")
    val userType: String,
    @SerialName("typeExplain")
    val typeExplain: String,
    @SerialName("testScores")
    val testScores: List<TestScoreDto>,
    @SerialName("subtype")
    val subtype: List<SubtypeDto>
)

@Serializable
data class TestScoreDto(
    val authoritative: Double,
    val authoritarian: Double,
    val permissive: Double
)

@kotlinx.serialization.Serializable
data class SubtypeDto(
    val type: String,
    val score: Double
)

@kotlinx.serialization.Serializable
data class MembershipResponseDto(
    val userId: Long
)