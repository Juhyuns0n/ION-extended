package com.ion.app.data.mapper.home

import com.ion.app.data.dto.response.home.HomeResponseDto
import com.ion.app.data.dto.response.home.RewardDto
import com.ion.app.domain.model.home.HomeMainModel
import com.ion.app.domain.model.home.RewardModel

fun HomeResponseDto.toDomain(): HomeMainModel =
    HomeMainModel(
        userImage = userImage,
        level = level,
        points = points,
        parentNickname = parentNickname,
        streakDay = streakDay,
        phrase = phrase,
        monthFrequency = monthFrequency,
        voicereportFrequency = voicereportFrequency,
        chatBotFrequency = chatBotFrequency,
        workBookFrequency = workBookFrequency,
        message = message,
        rewards = reward.map { it.toDomain() }
    )

fun RewardDto.toDomain(): RewardModel =
    RewardModel(
        rewardId = rewardId,
        earnedAt = earnedAt
    )