package com.ion.app.presentation.home.navigation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ion.app.R
import com.ion.app.core.util.buildImageUrl
import com.ion.app.domain.model.home.RewardModel
import com.ion.app.domain.repository.home.HomeRepository
import com.ion.app.presentation.home.HomeUiState
import com.ion.app.presentation.home.RewardItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _ui

    init {
        loadHome()
    }

    private fun loadHome() {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, errorMessage = null) }

            repository.loadHome()
                .onSuccess { home ->
                    _ui.update {
                        it.copy(
                            isLoading = false,
                            userImagePath = home.userImage,
                            level = home.level,
                            points = home.points,
                            parentNickname = home.parentNickname,
                            streakDay = home.streakDay,
                            phrase = home.phrase,
                            monthFrequency = home.monthFrequency,
                            voicereportFrequency = home.voicereportFrequency,
                            chatBotFrequency = home.chatBotFrequency,
                            workBookFrequency = home.workBookFrequency,
                            message = home.message,
                            rewards = buildUiRewards(home.rewards)
                        )
                    }
                    Log.d("HOME", "raw userImage = ${home.userImage}")
                    Log.d("HOME", "full url      = ${buildImageUrl(home.userImage)}")
                }
                .onFailure { e ->
                    Log.e("HomeViewModel", "loadHome failed", e)
                    _ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = e.message ?: "메인 정보를 불러오지 못했습니다."
                        )
                    }
                }
        }
    }

    private fun RewardModel.toUiRewardItem(): RewardItem =
        when (rewardId) {
            1 -> RewardItem(R.drawable.reward_heart, "육아 새싹", id = 1, earned = true)
            2 -> RewardItem(R.drawable.reward_book, "육아 입문자", id = 2, earned = true)
            3 -> RewardItem(R.drawable.reward_globe, "육아 중수", id = 3, earned = true)
            4 -> RewardItem(R.drawable.reward_moon, "소통의 탐험가", id = 4, earned = true)
            5 -> RewardItem(R.drawable.reward_tent, "질문의 모험가", id = 5, earned = true)
            6 -> RewardItem(R.drawable.reward_balloons, "육아 고수", id = 6, earned = true)
            else -> RewardItem(R.drawable.reward_heart, "리워드", id = rewardId, earned = true)
        }

    private fun buildUiRewards(domainRewards: List<RewardModel>): List<RewardItem> {
        val earnedIds = domainRewards.map { it.rewardId }.toSet()

        val all = listOf(
            RewardItem(R.drawable.reward_heart, "육아 새싹", id = 1, earned = earnedIds.contains(1)),
            RewardItem(R.drawable.reward_book, "육아 입문자", id = 5, earned = earnedIds.contains(5)),
            RewardItem(R.drawable.reward_moon, "소통의 탐험가", id = 4, earned = earnedIds.contains(4)),
            RewardItem(R.drawable.reward_globe, "육아 중수", id = 2, earned = earnedIds.contains(2)),
            RewardItem(R.drawable.reward_tent, "질문의 모험가", id = 6, earned = earnedIds.contains(6)),
            RewardItem(R.drawable.reward_balloons, "육아 고수", id = 3, earned = earnedIds.contains(3))
        )

        return all
    }
}