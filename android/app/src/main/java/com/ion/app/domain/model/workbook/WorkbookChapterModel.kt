package com.ion.app.domain.model.workbook

data class WorkbookChapterModel(
    val chapterId: Int,
    val chapterTitle: String,
    val chapterProgress: Int,
    val lessons: List<LessonSummary>,
    val totalPages: Int,
    val size: Int
) {
    data class LessonSummary(
        val lessonId: Int,
        val lessonTitle: String,
        val progressStatus: Int // 1: 완료, 0: 미완료
    )
}
