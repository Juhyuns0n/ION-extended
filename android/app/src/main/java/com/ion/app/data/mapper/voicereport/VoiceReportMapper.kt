package com.ion.app.data.mapper.voicereport

import android.util.Log
import com.ion.app.data.dto.response.voicereport.RecentVoiceSummaryDto
import com.ion.app.data.dto.response.voicereport.VoiceReportListItemDto
import com.ion.app.data.dto.response.voicereport.VoiceReportResponseDto
import com.ion.app.domain.model.voicereport.ChangeProposal
import com.ion.app.domain.model.voicereport.Emotion
import com.ion.app.domain.model.voicereport.Expression
import com.ion.app.domain.model.voicereport.Frequency
import com.ion.app.domain.model.voicereport.RecentVoiceSummaryModel
import com.ion.app.domain.model.voicereport.TimelineItem
import com.ion.app.domain.model.voicereport.VoiceReportListItemModel
import com.ion.app.domain.model.voicereport.VoiceReportModel


fun VoiceReportResponseDto.toDomain(): VoiceReportModel {
    return VoiceReportModel(
        id = reportId,
        subTitle = subTitle,
        day = day,
        conversationSummary = conversationSummary,
        overallFeedback = overallFeedback,
        expression = Expression(
            parentExpression = expression.parentExpression,
            kidExpression = expression.kidExpression,
            parentConditions = expression.parentConditions,
            kidConditions = expression.kidConditions,
            expressionFeedback = expression.expressionFeedback
        ),
        changeProposal = changeProposal.map {
            Log.d("VoiceReportMapper", "ChangeProposal: ${it.existingExpression} → ${it.proposalExpression}")
            ChangeProposal(
                existingExpression = it.existingExpression,
                proposalExpression = it.proposalExpression
            )
        },
        emotion = Emotion(
            timeline = emotion.timeline.map {
                Log.d("VoiceReportMapper", "Emotion timeline: ${it.time} / ${it.momentEmotion}")
                TimelineItem(
                    time = it.time,
                    momentEmotion = it.momentEmotion
                )
            },
            emotionFeedback = emotion.emotionFeedback
        ),
        kidAttitude = kidAttitude,
        frequency = Frequency(
            parentFrequency = frequency.parentFrequency,
            kidFrequency = frequency.kidFrequency,
            frequencyFeedback = frequency.frequencyFeedback
        ),
        strength = strength,
        kidsNickname = kidsNickname
    )
}

fun VoiceReportListItemDto.toDomain(): VoiceReportListItemModel =
    VoiceReportListItemModel(
        id = id,
        subTitle = subTitle,
        day = day
    )

fun List<VoiceReportListItemDto>.toDomainList(): List<VoiceReportListItemModel> =
    map { it.toDomain() }

fun RecentVoiceSummaryDto.toDomain() = RecentVoiceSummaryModel(
    momentEmotion = momentEmotion,
    parentFrequency = parentFrequency,
    kidFrequency = kidFrequency,
    overallFeedback = overallFeedback
)
