package com.ion.app.data.dto.response.workbook

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorkbookChapterResponseDto(
    @SerialName("chapterTitle") val chapterTitle: String,
    @SerialName("chapterProgress") val chapterProgress: Int,
    @SerialName("lessons") val lessons: List<LessonItem>,
    @SerialName("totalPages") val totalPages: Int,
    @SerialName("size") val size: Int
) {
    @Serializable
    data class LessonItem(
        @SerialName("lessonId") val lessonId: Int,
        @SerialName("lessonTitle") val lessonTitle: String,
        @SerialName("progressStatus") val progressStatus: Int
    )
}