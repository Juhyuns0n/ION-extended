package com.ion.app.data.dto.response.voicereport

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VoiceReportListResponseDto(
    @SerialName("content") val content: List<VoiceReportListItemDto>,
    @SerialName("totalPages") val totalPages: Int,
    @SerialName("size") val size: Int
)

@Serializable
data class VoiceReportListItemDto(
    @SerialName("reportId") val id: Long,
    @SerialName("subTitle") val subTitle: String,
    @SerialName("day") val day: String
)

@Serializable
data class VoiceReportResponseDto(
    @SerialName("subTitle") val subTitle: String,
    @SerialName("day") val day: String,
    @SerialName("conversationSummary") val conversationSummary: String,
    @SerialName("overallFeedback") val overallFeedback: String,
    @SerialName("expression") val expression: ExpressionDto,
    @SerialName("changeProposal") val changeProposal: List<ChangeProposalDto>,
    @SerialName("emotion") val emotion: EmotionDto,
    @SerialName("kidAttitude") val kidAttitude: String,
    @SerialName("frequency") val frequency: FrequencyDto,
    @SerialName("strength") val strength: String,
    @SerialName("kidsNickname") val kidsNickname: String,
    @SerialName("reportId") val reportId: Long
)

@Serializable
data class ExpressionDto(
    @SerialName("parentExpression") val parentExpression: String,
    @SerialName("kidExpression") val kidExpression: String,
    @SerialName("parentConditions") val parentConditions: String,
    @SerialName("kidConditions") val kidConditions: String,
    @SerialName("expressionFeedback") val expressionFeedback: String
)

@Serializable
data class ChangeProposalDto(
    @SerialName("existingExpression") val existingExpression: String,
    @SerialName("proposalExpression") val proposalExpression: String
)

@Serializable
data class EmotionDto(
    @SerialName("timeline") val timeline: List<TimelineItemDto>,
    @SerialName("emotionFeedback") val emotionFeedback: String
)

@Serializable
data class TimelineItemDto(
    @SerialName("time") val time: String,
    @SerialName("momentEmotion") val momentEmotion: String
)

@Serializable
data class FrequencyDto(
    @SerialName("parentFrequency") val parentFrequency: Int,
    @SerialName("kidFrequency") val kidFrequency: Int,
    @SerialName("frequencyFeedback") val frequencyFeedback: String
)

@Serializable
data class RecentVoiceSummaryDto(
    @SerialName("momentEmotion") val momentEmotion: String,
    @SerialName("parentFrequency") val parentFrequency: Int,
    @SerialName("kidFrequency") val kidFrequency: Int,
    @SerialName("overallFeedback") val overallFeedback: String
)

