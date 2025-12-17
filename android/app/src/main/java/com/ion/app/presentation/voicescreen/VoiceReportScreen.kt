package com.ion.app.presentation.voicescreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.tehras.charts.piechart.PieChart
import com.github.tehras.charts.piechart.PieChartData
import com.github.tehras.charts.piechart.renderer.SimpleSliceDrawer
import com.ion.app.R
import com.ion.app.core.designsystem.ui.theme.beige
import com.ion.app.domain.model.voicereport.ChangeProposal
import com.ion.app.domain.model.voicereport.Emotion
import com.ion.app.domain.model.voicereport.Expression
import com.ion.app.domain.model.voicereport.Frequency
import com.ion.app.domain.model.voicereport.VoiceReportModel
import com.ion.app.presentation.voicescreen.navigation.VoiceViewModel

@Preview
@Composable
fun VoiceReportRoutePreview() {
    val fake = VoiceReportModel(
        id = 1,
        subTitle = "창의성 존중과 감정 인정의 균형",
        day = "2025년 09월 20일",
        conversationSummary = "부모가 손님 도착 전까지만 활동을 멈추자고 요구하자 아 이는 강하게 거부하며 자기 기준(잘 때까지 친구와 함께) 을 주장하고 즉시 나가려는 의사를 보였습니다. 부모는 규칙을 상기시키며 시간을 묻고 합의된 약속을 언급했으 나 감정 공감이나 구체적 선택지를 제시하지 않았습니다.",
        overallFeedback = "자유방임형 양육 스타일과 창의성 목표 사이의 균형이 필요해 보입니다.",
        expression = Expression("...", "...", "초반에는 부모의 걱정과 시간 제 약에서 초조가, 이어서 아이의 불 만과 부모의 단호함으로 짜증이 올라옵니다. 감정 인정과 한정된 선택 제시로 진정시키세요.", "...", "..."),
        changeProposal = emptyList(),
        emotion = Emotion(emptyList(), "초반에는 부모의 걱정과 시간 제 약에서 초조가, 이어서 아이의 불 만과 부모의 단호함으로 짜증이 올라옵니다. 감정 인정과 한정된 선택 제시로 진정시키세요."),
        kidAttitude = "",
        frequency = Frequency(60, 40, ""),
        strength = "",
        kidsNickname = "아이온"
    )
    VoiceReportScreen(fake, topPadding = 0.dp, bottomPadding = 0.dp)
}
@Preview(showBackground = true)
@Composable
fun EmotionVerticalTimelinePreview() {
    val sample = listOf(
        "0초"  to "분노",
        "7초"  to "위축",
        "15초" to "체념"
    )

    Box(
        modifier = Modifier
            .background(Color(0xFFFFF3C8))
            .padding(16.dp)
    ) {
        TimelineCardContainer(
            timelineData = sample
        )
    }
}
@Composable
fun TimelineCardContainer(
    timelineData: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3C8)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 20.dp)
        ) {
            EmotionVerticalTimeline(
                items = timelineData
            )
        }
    }
}
@Composable
fun EmotionVerticalTimeline(
    items: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    val axisColor = Color(0xFFCC8A5A)
    val dotColor = Color(0xFF9B5A2A)

    val rowHeight = 60.dp
    val dotSize = 10.dp

    Column(modifier = modifier) {

        items.forEachIndexed { index, (time, emotion) ->

            Row(
                modifier = Modifier.height(rowHeight),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // 1) 시간
                Text(
                    text = time,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF460B0B),
                    modifier = Modifier
                        .width(32.dp),
                    textAlign = TextAlign.End
                )

                Spacer(modifier = Modifier.width(5.dp))

                // 2) 축 + 점
                Box(
                    modifier = Modifier
                        .width(15.dp)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    // 축: 위/아래 줄 연결 (맨 첫줄/막줄 고려)
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(axisColor)
                    )

                    // 점
                    Box(
                        modifier = Modifier
                            .size(dotSize)
                            .background(dotColor, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(15.dp))

                // 3) 감정 칩
                val chipColor =
                    if (index % 2 == 0) Color(0xFFD1C4E9) else Color(0xFFFFCDD2)

                Box(
                    modifier = Modifier
                        .background(chipColor, RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = emotion,
                        fontSize = 13.sp,
                        color = Color(0xFF4D0909)
                    )
                }
            }
        }
    }
}

@Composable
fun VoiceReportRoute(
    modifier: Modifier = Modifier,
    topPadding: Dp,
    bottomPadding: Dp,
    reportId: Long?,
    viewModel: VoiceViewModel = hiltViewModel()
) {
//    // ViewModel 초기화 시 reportId 전달
//    reportId?.let {
//        viewModel.loadVoiceReport(it)
//    }

    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.isLoading -> {
            // 전체 화면 로딩
            VoiceLoadingScreen()
        }

        uiState.errorMessage != null -> {
            // 에러 화면 (필요하면 이것도 이쁘게 바꿔도 됨)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(beige),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.errorMessage ?: "오류가 발생했어요.",
                    color = Color(0xFFB00020)
                )
            }
        }

        else -> {
            uiState.selectedReport?.let {
                VoiceReportScreen(
                    report = it,
                    topPadding = topPadding,
                    bottomPadding = bottomPadding
                )
            } ?: VoiceLoadingScreen(message = "리포트 데이터가 없어요.")
        }
    }
}


@Composable
fun VoiceReportScreen(
    report: VoiceReportModel,
    navigateToVoice: () -> Unit = {},
    viewModel: VoiceViewModel? = null,
    topPadding: Dp,
    bottomPadding: Dp
) {
    val scrollState = rememberScrollState()
    val timelinePairs: List<Pair<String, String>> =
        if (report.emotion.timeline.isNotEmpty()) {
            report.emotion.timeline.map { timeline ->
                timeline.time.toSecondLabel() to timeline.momentEmotion
            }
        } else {
            listOf(
                "0초"  to "분노",
                "7초"  to "위축",
                "15초" to "체념"
            )
        }

    val bubbleMessage = report.emotion.emotionFeedback

    Box(
        Modifier
            .fillMaxSize()
            .background(beige)
            .padding(top = topPadding, bottom = bottomPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(beige)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(30.dp))
            // 제목
            Text(
                text = "${report.kidsNickname}의 ${report.id}번째 보고서",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF000000)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = report.subTitle,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontSize = 18.sp,
                color = Color(0xFFFF7759),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(15.dp))
            Text(
                text = report.day,
                modifier = Modifier.align(Alignment.End),
                fontSize = 15.sp,
                color = Color(0xFF4D0909)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 카드 섹션 (대화 요약)
            SectionCard(
                background = R.drawable.img_rectangle2,
                title = "대화 요약",
                content = report.conversationSummary,
                contentPadding = PaddingValues(top = 27.dp, bottom = 50.dp, start = 24.dp, end = 24.dp)
            )

            SectionCard(
                background = R.drawable.img_rectangle3,
                title = "상호작용 전반 피드백",
                content = report.overallFeedback
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "나의 감정 분석",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(9.dp))

            // 타임라인 카드 + 말풍선 블럭
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp),
                contentAlignment = Alignment.Center          // 전체 블럭을 가운데로
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()                  // 자식 크기만큼만 차지
                        .heightIn(min = 180.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        0.dp
                    ),                                      // 카드와 말풍선 사이 간격
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 왼쪽 타임라인 카드 (노란 카드 전체도 가운데에 옴)
                    TimelineCardContainer(
                        timelineData = timelinePairs,
                        modifier = Modifier
                            .heightIn(min = 160.dp)
                            .weight(0.5f)
                    )

                    // 오른쪽 말풍선
                    SpeechBubbleWithImage(
                        message = bubbleMessage,
                        modifier = Modifier
                            .heightIn(min = 160.dp)
                            .weight(0.70f)           // 너무 넓어지지 않게
                    )
                }
            }


            Spacer(modifier = Modifier.height(25.dp))

            SituationExpressionSection(expression = report.expression)

            AlternativeExpressionSection(changeProposals = report.changeProposal)

            Spacer(modifier = Modifier.height(16.dp))

            SectionCard(
                background = R.drawable.img_rectangle4,
                title = "잘한 점 분석",
                content = report.strength,
                contentPadding = PaddingValues(
                    start = 18.dp,
                    end = 18.dp,
                    top = 10.dp,
                    bottom = 10.dp
                )
            )

            SectionCard(
                background = R.drawable.img_rectangle4,
                title = "아이의 대화 태도 분석",
                content = report.kidAttitude,
                contentPadding = PaddingValues(
                    start = 18.dp,
                    end = 18.dp,
                    top = 10.dp,
                    bottom = 10.dp
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 발화 비율 Section
            Text(
                text = "발화 빈도 분석",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(17.dp))

            // 그래프
            SpeechProportionSection(
                parentFrequency = report.frequency.parentFrequency,
                kidFrequency = report.frequency.kidFrequency,
                frequencyFeedback = report.frequency.frequencyFeedback
            )
        }
    }
}

@Composable
fun SituationExpressionSection(expression: Expression) {
    Text(
        text = "상황 / 표현 분석",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                // 전체 분석
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Image(
                        painter = painterResource(R.drawable.img_rectangle5),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.FillBounds
                    )
                    Text(
                        text = expression.expressionFeedback,
                        fontSize = 14.sp,
                        color = Color(0xFF4D0909),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top=10.dp, bottom = 20.dp, start = 15.dp, end = 15.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 부모 발화
                DialogueSection(
                    imageRes = R.drawable.img_parent,
                    message = expression.parentExpression,
                    analysisText = expression.parentConditions
                )

                // 아이 발화
                DialogueSection(
                    imageRes = R.drawable.img_child,
                    message = expression.kidExpression,
                    analysisText = expression.kidConditions
                )

            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))
}


@Composable
fun AlternativeExpressionSection(changeProposals: List<ChangeProposal>) {
    // 제목
    Text(
        text = "대체 표현 제안",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    Spacer(modifier = Modifier.padding(7.dp))

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        changeProposals.forEach { proposal ->
            AlternativeExpressionItem(
                original = proposal.existingExpression,
                suggestion = proposal.proposalExpression
            )
            Spacer(modifier = Modifier.height(14.dp))
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
fun SectionCard(background: Int, title: String, content: String, contentPadding: PaddingValues = PaddingValues(start = 24.dp, end = 24.dp, top = 15.dp, bottom = 25.dp)) {
    Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    Spacer(modifier = Modifier.height(15.dp))
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(), // Card의 세로 크기 지정(필요에 맞게 조정)
            contentAlignment = Alignment.TopCenter
        ) {
            Image(
                painter = painterResource(background),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )
            Column(modifier = Modifier.padding(paddingValues = contentPadding),
            ) {
                Text(text = content, fontSize = 14.sp, color = Color(0xFF444444))
            }
        }
    }

    Spacer(modifier = Modifier.height(30.dp))
}

@Composable
fun TimelineItemCard(time: String, feeling: String, index: Int) {
    val bgColor = if (index % 2 == 0) Color(0xFFD1C4E9) else Color(0xFFF8BBD0) // 보라/핑크 번갈아
    Row(
        modifier = Modifier
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = time,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF460B0B),
            modifier = Modifier.width(50.dp)
        )
        Box(
            modifier = Modifier
                .width(60.dp)
                .background(bgColor, shape = RoundedCornerShape(50))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = feeling,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Composable
fun SpeechBubbleExpression(usage: Int, message: String) {
    val backgroundPainter = when (usage) {
        1 -> painterResource(R.drawable.img_speechbubble_ogexpression)
        0 -> painterResource(R.drawable.img_speechbubble)
        else -> painterResource(R.drawable.img_speechbubble)
    }
    Box(
        modifier = Modifier
            .paint(
                painter = backgroundPainter,
                contentScale = ContentScale.FillBounds
            )
            .fillMaxWidth()
            .padding(top = 15.dp, bottom = 20.dp, start = 55.dp, end = 30.dp),
            contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = message,
            fontSize = 13.sp,
            lineHeight = 17.sp
        )
    }
}

@Composable
fun SpeechBubbleWithImage(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,                  // 여기서 width/height 이미 정해짐
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()                // weight로 받은 영역을 꽉 채움
        ) {
            // 말풍선 배경
            Image(
                painter = painterResource(R.drawable.img_speechbubble2),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )

            // 말풍선 안 텍스트
            Text(
                text = message,
                fontSize = 12.sp,
                color = Color(0xFF4D0909),
                softWrap = true,
                lineHeight = 16.sp,
                overflow = TextOverflow.Clip,
                maxLines = Int.MAX_VALUE,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        start = 42.dp,        // 꼬리/얼굴 피해서
                        end   = 20.dp,
                        top   = 18.dp,
                        bottom = 22.dp
                    )
            )
        }
    }
}

@Composable
fun DialogueSection(
    imageRes: Int,             // 부모/아이 이미지 리소스
    message: String,           // 말풍선 텍스트
    analysisText: String,      // 감정 분석 텍스트
    imageSize: Dp = 80.dp,     // 캐릭터 이미지 크기
    bubbleModifier: Modifier = Modifier,  // 필요시 조정
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // 캐릭터 + 말풍선
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = null,
                modifier = Modifier.size(imageSize)
            )
            Spacer(modifier = Modifier.width(10.dp))
            SpeechBubbleExpression(
                usage = 0,
                message = message
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 분석 텍스트 (상황 요약)
        Box(
            modifier = Modifier
                .paint(painter = painterResource(R.drawable.img_rectangle_convo))
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                text = analysisText,
                fontSize = 13.sp,
                color = Color(0xFF4D0909),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun AlternativeExpressionItem(
    original: String,
    suggestion: String
) {
    // 대체 표현 제안 세트
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // 원래 표현
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(255.dp)
        ) {
            SpeechBubbleExpression(
                usage = 1,
                message = original
            )
        }

        // 화살표 + 대체 표현
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.padding(20.dp))
            Image(
                painter = painterResource(R.drawable.img_arrow),
                contentDescription = "Arrow",
                modifier = Modifier
                    .size(70.dp)
                    .offset(x = 30.dp, y = (-25).dp)
            )
            Spacer(modifier = Modifier.padding(10.dp))
            SpeechBubbleExpression(
                usage = 0,
                message = suggestion
            )
        }
    }
}

@Composable
fun SpeechProportionSection(parentFrequency: Int, kidFrequency: Int, frequencyFeedback: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val total = (parentFrequency + kidFrequency).takeIf { it > 0 } ?: 1
        val parentRatio = parentFrequency * 100f / total
        val kidRatio = kidFrequency * 100f / total

        // 왼쪽: Pie Chart
        PieChart(
            pieChartData = PieChartData(listOf(
                PieChartData.Slice(parentRatio, Color(0xFFFF7759)),
                PieChartData.Slice(kidRatio, Color(0xFFFFECA7))
            )),
            sliceDrawer = SimpleSliceDrawer(100F),
            modifier = Modifier.size(90.dp).offset(y = (-15).dp)
        )

        Spacer(modifier = Modifier.padding(end = 20.dp))

        // 오른쪽: SectionCard 설명 영역
        SectionCard(
            background = R.drawable.img_rectangle4,
            title = "",
            content = frequencyFeedback,
            contentPadding = PaddingValues(
                start = 18.dp,
                end = 18.dp,
                top = 10.dp,
                bottom = 10.dp
            )
        )
    }
    // PieChart 아래 라벨 영역
    Row(
        modifier = Modifier.fillMaxWidth().offset(y = (-20).dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LabelItem(
            color = Color(0xFFFF7759),
            label = "부모"
        )
        Spacer(modifier = Modifier.width(8.dp))
        LabelItem(
            color = Color(0xFFFFECA7),
            label = "아이"
        )
    }
}

@Composable
fun LabelItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(modifier = Modifier.width(3.dp))
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color = color, shape = RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(text = label, fontWeight = FontWeight.Medium, fontSize = 12.sp)
    }
}

private fun String.toSecondLabel(): String {
    val raw = this.trim()

    // 예: "00:07.0", "00:07s" 등에서 초 부분만 숫자만 남기기
    val lastPart = raw.split(":").lastOrNull()?.trim() ?: return this

    // 숫자만 추출 (앞 숫자만)
    val digitPart = lastPart.takeWhile { it.isDigit() }
    val sec = digitPart.toIntOrNull() ?: return this

    return "${sec}초"
}

@Composable
fun VoiceLoadingScreen(
    modifier: Modifier = Modifier,
    message: String = "리포트를 불러오는 중이에요..."
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(beige),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.CircularProgressIndicator(
                color = Color(0xFFFF7759)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color(0xFF4D0909)
            )
        }
    }
}
