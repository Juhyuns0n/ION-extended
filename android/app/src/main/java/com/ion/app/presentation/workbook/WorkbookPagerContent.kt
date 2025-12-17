package com.ion.app.presentation.workbook

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.ion.app.presentation.workbook.screen.WorkbookLessonDescriptiveScreen
import com.ion.app.presentation.workbook.screen.WorkbookLessonOptionScreen


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkbookPagerContent(
    chapterId: Int,
    pagerState: PagerState,
    uiState: WorkbookUiState,
    onSelectOption: (page: Int, selectedIndex: Int) -> Unit,
    onTextAnswerChange: (Int, String) -> Unit,
    onNext: () -> Unit,
    navigateToChapter: (Int) -> Unit
) {
    if (uiState.activityList.isEmpty()) {
        Text(
            text = "활동을 불러오는 중이에요...",
            fontSize = 16.sp,
            color = Color(0xFF666666),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        return
    }

    HorizontalPager(state = pagerState) { page ->
        val activity = uiState.activityList[page]

        // 이 페이지가 마지막 액티비티인지
        val isLast = page == uiState.activityList.lastIndex

        // 버튼 활성 여부: 피드백 로딩 중이거나 이미 피드백 있으면 막기
        val isNextEnabled = !uiState.isFeedbackLoading && !uiState.hasFeedback

        // 버튼 라벨만 상황에 맞게 계산
        val nextLabel = when {
            uiState.isFeedbackLoading -> "피드백 받는 중…"
            isLast -> "완료"
            else -> "다음"
        }

        when (activity.type) {
            "write" -> {
                WorkbookLessonDescriptiveScreen(
                    question = activity.instruction,
                    answer = activity.yourResponse.orEmpty(),
                    onAnswerChange = { newText ->
                        onTextAnswerChange(page, newText)
                    },
                    onNext = onNext,
                    isNextEnabled = isNextEnabled,
                    nextButtonLabel = nextLabel
                )
            }

            "select" -> {
                WorkbookLessonOptionScreen(
                    question = activity.instruction,
                    options = activity.options,
                    selectedIndex = activity.yourResponse?.toIntOrNull(),
                    onSelect = { idx ->
                        onSelectOption(page, idx)
                    },
                    onNext = onNext,
                    isNextEnabled = isNextEnabled,
                    nextButtonLabel = nextLabel
                )
            }

            else -> {
                Text(
                    text = activity.instruction,
                    fontSize = 16.sp,
                    color = Color(0xFF333333)
                )
            }
        }
    }
}
