package com.ion.app.presentation.workbook

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ion.app.R
import com.ion.app.core.designsystem.ui.theme.IonTheme
import com.ion.app.core.designsystem.ui.theme.beige
import com.ion.app.presentation.workbook.navigation.WorkbookViewModel


@Composable
fun WorkbookSectionInfoRoute(
    modifier: Modifier = Modifier,
    topPadding: Dp,
    bottomPadding: Dp,
    chapterId: Int,
    viewModel: WorkbookViewModel = hiltViewModel(),
    navigateToChapter: (Int) -> Unit
) {
    // 필요한 상태 및 UI 로직이 있다면 여기에 (예: viewModel.uiState, pagerState 등)
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 }) // 예시, 실제 앱에 맞게 구현
    val uiState by viewModel.uiState.collectAsState()

    androidx.compose.runtime.LaunchedEffect(chapterId) {
        viewModel.loadChapterTheory(chapterId)
    }

    WorkbookSectionInfoScreen(
        uiState = uiState,
        topPadding = topPadding
    )
}

@Composable
fun WorkbookSectionInfoScreen(
    uiState: WorkbookUiState,
    topPadding: Dp
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(color = beige)
            .padding(top = topPadding)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Spacer(modifier = Modifier.height(35.dp))

            Image(
                painter = painterResource(lessonImageResForChapter(uiState.chapterId?:1)),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Section 번호/제목
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "단원 ${uiState.chapterId ?: "-"}",
                        style = IonTheme.typography.body2,
                        color = IonTheme.colors.black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 서버에서 온 chapterTitle
            Text(
                text = uiState.chapterName ?: "",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp, top = 0.dp)
            )

            // ============ 블럭 1: 필요성(necessity) ============
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.img_checkboard),
                    contentDescription = null,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("왜 중요한가?", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text = uiState.theoryNecessity
                    ?: "이 단원이 왜 중요한지에 대한 설명을 불러오는 중입니다.",
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                modifier = Modifier.padding(start = 20.dp)
            )

            Spacer(Modifier.size(30.dp))

            // ============ 블럭 2: 학습 목표(studyGoal) ============
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.img_checkboard),
                    contentDescription = null,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("이 단원에서 무엇을 배우나요?", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text = uiState.theoryStudyGoal
                    ?: "이 단원의 학습 목표를 불러오는 중입니다.",
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                modifier = Modifier.padding(start = 20.dp)
            )

            Spacer(Modifier.height(30.dp))

            // ============ 블럭 3: notion(핵심 개념) 카드 ============
            Box(
                modifier = Modifier
                    .shadow(
                        2.dp,
                        RoundedCornerShape(16.dp),
                        ambientColor = Color(0xFF772A0B).copy(alpha = 0.13f)
                    )
                    .fillMaxWidth()
                    .heightIn(min = 106.dp)
                    .background(Color(0xFFFFE0ED), RoundedCornerShape(16.dp))
            ) {
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "핵심 개념",
                        color = Color(0xF0000000),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(start = 20.dp, top = 16.dp)
                    )
                    Text(
                        text = uiState.theoryNotion
                            ?: "이 단원의 핵심 개념을 불러오는 중입니다.",
                        color = Color(0xF0000000),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .padding(start = 28.dp, top = 6.dp, bottom = 16.dp, end = 20.dp)
                    )
                }
            }
        }
    }
}

@Preview(
    name = "Workbook Section Info Screen Preview",
    showBackground = true,
    backgroundColor = 0xFFEFE6DC,
    widthDp = 390,
    heightDp = 844
)
@Composable
fun PreviewWorkbookSectionInfoScreen() {
    IonTheme {
        WorkbookSectionInfoScreen(
            uiState = WorkbookUiState(
                chapterId = 1,
                chapterName = "자기 인식: 부모로서의 나 이해하기",
                theoryNecessity = "부모로서 자신의 감정을 이해해야 아이의 감정을 더 잘 공감할 수 있어요.",
                theoryStudyGoal = "나의 감정 패턴을 인식하고 건강하게 표현하는 방법을 배우는 것이 목표예요.",
                theoryNotion = "자기 인식은 자신의 생각, 감정, 행동을 있는 그대로 바라보고 이해하는 능력을 말해요."
            ),
            topPadding = 0.dp
        )
    }
}