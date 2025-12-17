package com.ion.app.data.dto.response.workbook

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorkbookLessonResponseDto(
    @SerialName("chapterTitle") val chapterTitle: String,
    @SerialName("lessonTitle") val lessonTitle: String,

    // 서술형 2개
    @SerialName("firstDescriptiveFormQuestion")
    val firstDescriptiveFormQuestion: String,
    @SerialName("firstDescriptiveFormExample")
    val firstDescriptiveFormExample: String,

    @SerialName("secondDescriptiveFormQuestion")
    val secondDescriptiveFormQuestion: String,
    @SerialName("secondDescriptiveFormExample")
    val secondDescriptiveFormExample: String,

    // 선택형 3개
    @SerialName("firstSelectiveQuestion")
    val firstSelectiveQuestion: String,
    @SerialName("firstSelectiveOptions")
    val firstSelectiveOptions: List<String>,
    @SerialName("firstSelectiveExample")
    val firstSelectiveExample: String,

    @SerialName("secondSelectiveQuestion")
    val secondSelectiveQuestion: String,
    @SerialName("secondSelectiveOptions")
    val secondSelectiveOptions: List<String>,
    @SerialName("secondSelectiveExample")
    val secondSelectiveExample: String,

    @SerialName("thirdSelectiveQuestion")
    val thirdSelectiveQuestion: String,
    @SerialName("thirdSelectiveOptions")
    val thirdSelectiveOptions: List<String>,
    @SerialName("thirdSelectiveExample")
    val thirdSelectiveExample: String,

    // 안 씀
    @SerialName("workbookId") val workbookId: Long? = null,
    @SerialName("lessonId") val lessonId: Long? = null,
    @SerialName("firstDescriptiveFormAnswer") val firstDescriptiveFormAnswer: String? = null,
    @SerialName("secondDescriptiveFormAnswer") val secondDescriptiveFormAnswer: String? = null,
    @SerialName("firstSelectiveAnswer") val firstSelectiveAnswer: String? = null,
    @SerialName("secondSelectiveAnswer") val secondSelectiveAnswer: String? = null,
    @SerialName("thirdSelectiveAnswer") val thirdSelectiveAnswer: String? = null,
    @SerialName("workbookFeedback") val workbookFeedback: String? = null
)
