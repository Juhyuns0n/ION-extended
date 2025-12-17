package com.ion.app.data.dto.response.home

import kotlinx.serialization.Serializable

@Serializable
data class HomeResponseDto(
    val userImage: String? = null,
    val level: Int,
    val points: Int,
    val parentNickname: String,
    val streakDay: Int,
    val phrase: String,
    val monthFrequency: Int,
    val voicereportFrequency: Int,
    val chatBotFrequency: Int,
    val workBookFrequency: Int,
    val message: String,
    val reward: List<RewardDto> = emptyList()
)

@Serializable
data class RewardDto(
    val rewardId: Int,
    val earnedAt: String
)

@Serializable
data class FailMainResponseDto(
    val msg: String
)