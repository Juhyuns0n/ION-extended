package com.ion.app.presentation.workbook

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ion.app.R
import com.ion.app.core.designsystem.ui.theme.IonTheme
import com.ion.app.core.designsystem.ui.theme.beige
import com.ion.app.presentation.workbook.navigation.WorkbookViewModel
import com.ion.app.presentation.workbook.screen.SimulationLessonScreen
import kotlinx.coroutines.launch

@Composable
fun WorkbookLessonRoute(
    modifier: Modifier = Modifier,
    bottomPadding: Dp,
    viewModel: WorkbookViewModel = hiltViewModel(),
    navigateToChapter: (Int) -> Unit,
    navigateToWorkbook: () -> Unit,
    topPadding: Dp,
    chapterId: Int,
    lessonId: Long
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { uiState.activityList.size.coerceAtLeast(1) }
    )
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(chapterId, lessonId) {
        viewModel.loadLesson(chapterId, lessonId)

        if (lessonId != 5L) {
            viewModel.loadWorkbookFeedback(
                chapterId = chapterId,
                lessonId = lessonId,
                showError = false
            )
        }
    }

    when {
        uiState.isLoading -> {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(top = topPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading...")
            }
            return
        }

        uiState.errorMessage != null -> {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(top = topPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: ${uiState.errorMessage}")
            }
            return
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(beige)
    ) {
        // 레슨 화면
        WorkbookLessonScreen(
            navigateToChapter = navigateToChapter,
            navigateToWorkbook = navigateToWorkbook,
            pagerState = pagerState,
            uiState = uiState,
            onSelectOption = { page, selected ->
                viewModel.selectOption(page, selected)
            },
            onTextAnswerChange = { index, newText ->
                viewModel.enterTextAnswer(index, newText)
            },
            onNext = {
                if (pagerState.currentPage < uiState.activityList.size - 1) {
                    coroutineScope.launch {
                        pagerState.scrollToPage(pagerState.currentPage + 1)
                    }
                } else {
                    // 마지막 페이지 → 답안 제출 + 피드백 요청
                    viewModel.postWorkbookAnswer()
                }
            },
            onSimulationSend = { userText ->
                viewModel.sendSimulationLine(userText)
            },
            onSimulationFinish = { viewModel.loadSimulationFeedback(chapterId) },
            topPadding = topPadding
        )

        // 피드백 도착 시 오버레이
        if (uiState.hasFeedback) {
            FeedbackOverlay(
                feedbackText = uiState.feedbackText,
                onConfirm = {
                    navigateToChapter(chapterId)
                }
            )
        }
    }
}

@Composable
fun WorkbookLessonScreen(
    navigateToWorkbook: () -> Unit = {},
    navigateToChapter: (Int) -> Unit = {},
    pagerState: PagerState,
    uiState: WorkbookUiState,
    onSelectOption: (page: Int, selectedIndex: Int) -> Unit,
    onTextAnswerChange: (Int, String) -> Unit,
    onNext: () -> Unit,
    onSimulationSend: (String) -> Unit,
    onSimulationFinish: () -> Unit,
    topPadding: Dp
) {
    val chapterId = uiState.chapterId ?: 1
    val lessonId = uiState.lessonId
    val dayLabel = lessonId?.let { " - day $it" } ?: ""

    // ✅ 마지막 페이지 여부 (시뮬레이션이 아닐 때만)
    val isLastPage =
        !uiState.isSimulation &&
                uiState.activityList.isNotEmpty() &&
                pagerState.currentPage == uiState.activityList.lastIndex

    Box(
        Modifier
            .fillMaxSize()
            .background(color = beige)
            .padding(top = topPadding)
    ) {
        // 메인 컨텐츠
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.width(143.dp)) {
                    Text(
                        text = "Chapter $chapterId",
                        style = IonTheme.typography.body2,
                        color = IonTheme.colors.black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }
                Box(modifier = Modifier.width(143.dp)) {
                    Text(
                        text = dayLabel,
                        style = IonTheme.typography.body2,
                        color = Color(0xFFFF7759),
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            Text(
                text = uiState.lessonName.orEmpty(),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFF4D0909),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp, top = 0.dp)
            )

            Image(
                painter = painterResource(lessonImageResForChapter(chapterId)),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                Modifier
                    .shadow(
                        2.dp,
                        RoundedCornerShape(16.dp),
                        ambientColor = Color(0xFF772A0B).copy(alpha = 0.13f)
                    )
                    .fillMaxWidth()
                    .heightIn(min = 200.dp)
                    .background(Color(0xFFFDF2CA), RoundedCornerShape(16.dp))
                    .padding(vertical = 18.dp, horizontal = 14.dp)
            ) {
                if (uiState.isSimulation) {
                    SimulationLessonScreen(
                        situation = uiState.simulationSituationExplain.orEmpty(),
                        dialogues = uiState.simulationDialogues,
                        onSend = { userText -> onSimulationSend(userText) },
                        onFinish = { onSimulationFinish() },
                        isSending = uiState.isSimulationSending,
                        isFinished = uiState.isSimulationFinished
                    )
                } else {
                    WorkbookPagerContent(
                        chapterId = chapterId,
                        pagerState = pagerState,
                        uiState = uiState,
                        onSelectOption = onSelectOption,
                        onTextAnswerChange = onTextAnswerChange,
                        onNext = onNext,
                        navigateToChapter = navigateToChapter
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 피드백 로딩 오버레이
        if (uiState.isFeedbackLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x66000000)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "피드백을 불러오는 중이에요...",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun FeedbackOverlay(
    feedbackText: String,
    onConfirm: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x66000000)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .padding(start = 32.dp, bottom = 35.dp)
            ) {
                // 분홍 말풍선
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFFFFCCD8),
                            shape = RoundedCornerShape(28.dp)
                        )
                        .padding(
                            start = 24.dp,
                            end = 20.dp,
                            top = 20.dp,
                            bottom = 38.dp
                        )
                ) {
                    Text(
                        text = feedbackText,
                        fontSize = 14.sp,
                        color = Color(0xFF4E4636),
                        lineHeight = 20.sp
                    )
                }

                Spacer(Modifier.height(16.dp))

                // 확인 버튼
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    androidx.compose.material3.Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(5.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF7759),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .width(100.dp)
                            .height(38.dp)
                    ) {
                        Text(
                            text = "확인",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Image(
                painter = painterResource(id = R.drawable.ic_rabbit),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = (-28).dp, y = 18.dp)
            )
        }
    }
}

@Preview(
    name = "Workbook Feedback Overlay",
    showBackground = true,
    backgroundColor = 0xFFEFE6DC
)
@Composable
fun FeedbackOverlayPreview() {
    IonTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(beige)
        ) {
            FeedbackOverlay(
                feedbackText = "지돌이의 감정을 먼저 인정하고 공감해주는 부분이 훌륭합니다. " +
                        "특히 '속상했겠네'라고 말한 부분은 지돌이의 감정을 이해하려는 노력으로 보여 칭찬합니다. " +
                        "다만, 상황을 해결하는 방법에 대한 의견을 제시하기 전에 지돌이의 생각을 먼저 물어보는 것도 좋습니다.",
                onConfirm = { }
            )
        }
    }
}

@DrawableRes
fun lessonImageResForChapter(chapterId: Int): Int =
    when (chapterId) {
        1 -> R.drawable.img_lesson1
        2 -> R.drawable.img_lesson2
        3 -> R.drawable.img_lesson3
        4 -> R.drawable.img_lesson4
        5 -> R.drawable.img_lesson5
        6 -> R.drawable.img_lesson6
        7 -> R.drawable.img_lesson7
        else -> R.drawable.img_lesson1
    }
