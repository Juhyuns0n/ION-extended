package com.ion.app.domain.model.home

data class HomeMainModel(
    val userImage: String?,
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
    val rewards: List<RewardModel>
)

data class RewardModel(
    val rewardId: Int,
    val earnedAt: String
)