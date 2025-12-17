package com.ion.app.domain.model.voicereport


data class VoiceReportModel(
    val id: Long,
    val subTitle: String,
    val day: String,
    val conversationSummary: String,
    val overallFeedback: String,
    val expression: Expression,
    val changeProposal: List<ChangeProposal>,
    val emotion: Emotion,
    val kidAttitude: String,
    val frequency: Frequency,
    val strength: String,
    val kidsNickname: String
)

data class Expression(
    val parentExpression: String,
    val kidExpression: String,
    val parentConditions: String,
    val kidConditions: String,
    val expressionFeedback: String
)

data class ChangeProposal(
    val existingExpression: String,
    val proposalExpression: String
)

data class Emotion(
    val timeline: List<TimelineItem>,
    val emotionFeedback: String
)

data class TimelineItem(
    val time: String,
    val momentEmotion: String
)

data class Frequency(
    val parentFrequency: Int,
    val kidFrequency: Int,
    val frequencyFeedback: String
)

data class RecentVoiceSummaryModel(
    val momentEmotion: String,
    val parentFrequency: Int,
    val kidFrequency: Int,
    val overallFeedback: String
)
