package com.ion.app.data.datasourceimpl.local

import android.util.Log
import com.ion.app.data.datasource.local.voicereport.VoiceReportLocalDataSource
import com.ion.app.domain.model.voicereport.ChangeProposal
import com.ion.app.domain.model.voicereport.Emotion
import com.ion.app.domain.model.voicereport.Expression
import com.ion.app.domain.model.voicereport.Frequency
import com.ion.app.domain.model.voicereport.TimelineItem
import com.ion.app.domain.model.voicereport.VoiceReportModel
import okhttp3.MultipartBody
import javax.inject.Inject

class VoiceReportLocalDataSourceImpl @Inject constructor() : VoiceReportLocalDataSource {
    // 하드코딩 된 더미 데이터 리스트
    private val reports = mutableListOf(
        VoiceReportModel(
            id = 1,
            subTitle = "창의성 존중과 감정 인정의 균형",
            day = "2025년 09월 20일",
            conversationSummary = "부모가 아이의 학습을 강요하며 비난적인 태도를 보이자, 아이가 강하게 반발하고 있습니다.",
            overallFeedback = "자유방임형 양육 스타일과 창의성 목표 사이의 균형이 필요해 보입니다. ...",
            expression = Expression(
                parentExpression = "“못 하면 더 열심히 해야지“, “바보로 살거면 나가“",
                kidExpression = "“몰라, 안할거야“",
                parentConditions = "아이가 학습에 어려움을 겪거나 거부할 때 비난적인 표현을 사용하고 있습니다.",
                kidConditions = "부모의 강요와 비난에 대해 강하게 반발하고 거부하고 있습니다.",
                expressionFeedback = "아이의 감정을 인정하면서도 긍정적인 방향을 제시해보세요."
            ),
            changeProposal = listOf(
                ChangeProposal(
                    existingExpression = "“못 하면 더 열심히 해야지“",
                    proposalExpression = "지금 어려운 걸 알아, 어떻게 하면 더 재미있게 할 수 있을까?"
                ),
                ChangeProposal(
                    existingExpression = "“바보로 살거면 나가“",
                    proposalExpression = "조금 화났구나? 쉬었다가 다시 할까?"
                )
            ),
            emotion = Emotion(
                timeline = listOf(
                    TimelineItem(time = "00:15", momentEmotion = "답답함"),
                    TimelineItem(time = "00:30", momentEmotion = "짜증"),
                    TimelineItem(time = "00:45", momentEmotion = "분노"),
                    TimelineItem(time = "01:00", momentEmotion = "체념")
                ),
                emotionFeedback = "감정이 고조될 때는 잠시 ‘멈춤’을 실천해보세요. 심호흡을 3번 하고 지금 이 감정은 무엇인지 인지하고 대화를 이어가보세요"
            ),
            kidAttitude = "아이는 부모의 강요와 비난에 대해 강하게 반발적인 태도를 보이고 있어요. '안 치울래'라고 명확한 거부 의사를 표현합니다.",
            frequency = Frequency(
                parentFrequency = 60,
                kidFrequency = 40,
                frequencyFeedback = "부모님의 발화가 현저히 더 많습니다. 아이의 말을 더 경청하고 반응할 기회를 주세요. 하루 5분씩 아이가 하는 말을 경청하는 시간을 가져보는 건 어떨까요?"
            ),
            strength = "아이의 의사를 명확하게 표현하도록 했음은, 자유방임형 양육의 장점입니다. 이를 바탕으로 아이는 창의성과 표현력을 기를 수 있습니다.",
            kidsNickname = "아잉"
        ),
        VoiceReportModel(
            id = 2,
            subTitle = "협력적 대화의 시작",
            day = "2025년 09월 25일",
            conversationSummary = "부모가 아이와 숙제 계획을 함께 세우며 협력적인 분위기를 조성했습니다.",
            overallFeedback = "아이의 참여를 이끌어내는 긍정적인 대화입니다.",
            expression = Expression(
                parentExpression = "“우리 언제 하면 좋을까?“",
                kidExpression = "“저녁 먹고 할래!“",
                parentConditions = "함께 계획을 세우는 과정에서 존중을 표현하고 있습니다.",
                kidConditions = "자신의 의견이 반영되자 적극적인 태도를 보이고 있습니다.",
                expressionFeedback = "이런 대화가 자율성과 책임감을 함께 키워줍니다."
            ),
            changeProposal = emptyList(),
            emotion = Emotion(
                timeline = listOf(
                    TimelineItem(time = "00:10", momentEmotion = "평온"),
                    TimelineItem(time = "00:20", momentEmotion = "기대감")
                ),
                emotionFeedback = "긍정적인 감정 흐름이 잘 유지되었습니다."
            ),
            kidAttitude = "적극적이고 협조적인 태도를 보입니다.",
            frequency = Frequency(
                parentFrequency = 55,
                kidFrequency = 45,
                frequencyFeedback = "적절한 대화 비율입니다."
            ),
            strength = "상호 존중의 대화가 잘 이루어졌습니다.",
            kidsNickname = "아잉"
        ),
        VoiceReportModel(
            id = 3,
            subTitle = "감정 조절을 위한 대화",
            day = "2025년 10월 05일",
            conversationSummary = "아이가 친구와 다툰 후, 부모가 감정을 다루는 법을 함께 이야기했습니다.",
            overallFeedback = "감정 코칭의 좋은 예시입니다.",
            expression = Expression(
                parentExpression = "“화났을 때 어떻게 하면 좋을까?“",
                kidExpression = "“몰라, 그냥 짜증나!“",
                parentConditions = "감정을 다루는 방법을 유도하고 있습니다.",
                kidConditions = "감정 표현이 서툴지만 대화에 참여하려는 모습이 있습니다.",
                expressionFeedback = "감정을 있는 그대로 받아주며 다음 단계로 나아가보세요."
            ),
            changeProposal = listOf(
                ChangeProposal(
                    existingExpression = "“몰라, 그냥 짜증나!“",
                    proposalExpression = "짜증나는 건 당연해. 그럴 땐 어떻게 하면 조금 나아질까?"
                )
            ),
            emotion = Emotion(
                timeline = listOf(
                    TimelineItem(time = "00:12", momentEmotion = "짜증"),
                    TimelineItem(time = "00:25", momentEmotion = "슬픔"),
                    TimelineItem(time = "00:40", momentEmotion = "안정")
                ),
                emotionFeedback = "감정을 억누르지 않고 순차적으로 다룬 점이 좋습니다."
            ),
            kidAttitude = "초기에는 방어적이었지만 점차 대화에 참여했습니다.",
            frequency = Frequency(
                parentFrequency = 65,
                kidFrequency = 35,
                frequencyFeedback = "부모의 대화 비중이 높습니다. 아이의 말을 끝까지 들어주세요."
            ),
            strength = "감정 표현을 자연스럽게 유도했습니다.",
            kidsNickname = "아잉"
        )
    )
    override suspend fun uploadVoiceReport(audioFile: MultipartBody.Part): VoiceReportModel {
        Log.d("VoiceReportLocal", "uploadVoiceReport() called with file: ${audioFile.body.contentType()}")

        // 🔹 새로운 더미 리포트 자동 추가
        val newId = (reports.maxOfOrNull { it.id } ?: 0) + 1
        val newReport = VoiceReportModel(
            id = newId,
            subTitle = "새로운 분석 리포트 #$newId",
            day = "2025년 10월 14일",
            conversationSummary = "이 리포트는 업로드 테스트를 통해 자동 생성된 항목입니다.",
            overallFeedback = "실제 서버 업로드가 연결되면, 여기에 분석 결과가 표시됩니다.",
            expression = Expression(
                parentExpression = "“빨리 해!“",
                kidExpression = "“잠깐만~“",
                parentConditions = "조급한 상황에서 아이에게 지시함.",
                kidConditions = "놀라며 반응하지만 큰 갈등은 없음.",
                expressionFeedback = "조급함을 느낄 땐, 천천히 요청하는 방식으로 바꿔보세요."
            ),
            changeProposal = emptyList(),
            emotion = Emotion(emptyList(), "감정 데이터 없음"),
            kidAttitude = "테스트 리포트",
            frequency = Frequency(50, 50, "균형 잡힌 대화입니다."),
            strength = "테스트용 데이터입니다.",
            kidsNickname = "아잉"
        )

        reports.add(newReport)
        return newReport
    }

    override suspend fun getVoiceReports(): List<VoiceReportModel> = reports
    override suspend fun getVoiceReportById(id: Long): VoiceReportModel? =
        reports.find { it.id == id }
}