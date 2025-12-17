package com.ion.app.presentation.home

data class HomeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    val userImagePath: String? = null,
    val level: Int = 0,
    val points: Int = 0,
    val parentNickname: String = "",

    val streakDay: Int = 0,
    val phrase: String = "",

    val monthFrequency: Int = 0,
    val voicereportFrequency: Int = 0,
    val chatBotFrequency: Int = 0,
    val workBookFrequency: Int = 0,

    val message: String = "",

    val rewards: List<RewardItem> = emptyList()
)

data class RewardItem(
    val resId: Int,
    val label: String,
    val id: Int,
    val earned: Boolean
)