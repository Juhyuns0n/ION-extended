package com.ion.app.data.dto.response.workbook

import kotlinx.serialization.Serializable

@Serializable
data class SimulationLessonResponseDto(
    val simulationId: Long? = null,
    val done: Boolean? = null,
    val userId: Long? = null,
    val chapterId: Int? = null,
    val chapterTitle: String,
    val lessonId: Long? = null,
    val lessonTitle: String,
    val simulationSituationExplain: String,
    val dialogues: List<SimulationDialogueDto>
)

@Serializable
data class SimulationDialogueDto(
    val userLine: String? = null,
    val aiLine: String? = null
)