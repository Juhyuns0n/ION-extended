package com.ion.app.domain.model.workbook

data class SimulationLessonModel(
    val chapterId: Int,
    val lessonTitle: String,
    val situationExplain: String,
    val dialogues: List<Dialogue>
) {
    data class Dialogue(
        val userLine: String?,
        val aiLine: String?
    )
}
