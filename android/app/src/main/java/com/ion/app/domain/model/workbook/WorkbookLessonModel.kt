package com.ion.app.domain.model.workbook

data class WorkbookLessonModel(
    val chapterId: Int,
    val workbookId: Long,
    val workbookTitle: String,
    val activities: List<ActivityItem>
) {
    data class ActivityItem(
        val type: String,
        val instruction: String,
        val options: List<String>,
        val situation: String?,
        val yourResponse: String?,
        val optimalOption: String?,
        val optimalWriting: String?,
        val optimalSim: String?
    )
}