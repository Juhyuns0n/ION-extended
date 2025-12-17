package com.ion.app.presentation.workbook

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
fun WorkbookRoute(
    modifier: Modifier = Modifier,
    topPadding: Dp,
    bottomPadding: Dp,
    viewModel: WorkbookViewModel = hiltViewModel(),
    navigateToChapter: (Int) -> Unit,
    navigateToWorkbook: () -> Unit,
) {
    WorkbookScreen(
        navigateToChapter = navigateToChapter,
        topPadding = topPadding,
        bottomPadding = bottomPadding,
        workbookModel = viewModel
    )
}

@Composable
fun WorkbookScreen(
    navigateToWorkbook: () -> Unit = {},
    navigateToChapter: (Int) -> Unit = {},
    workbookModel: WorkbookViewModel? = null,
    topPadding: Dp,
    bottomPadding: Dp
) {
    // 단원 인덱스 (0 ~ 6)
    var chapterIndex by remember { mutableStateOf(0) }

    val chapterTitles = listOf(
        "자기 인식: 부모로서의 나 이해하기",
        "감정 코칭: 감정 인식, 표현, 수용",
        "의사소통: 나 전달법, 경청, 공감 훈련",
        "행동 지도: 규칙 설정, 일관성 있는 반응법",
        "스트레스 관리: 회복 탄력성, 자기 돌봄",
        "공감적 양육: 아이의 관점에서 바라보기",
        "발달 이해: 연령별 발달과 그에 맞는 대응"
    )
    val chapterNames = listOf(
        "나를 알아가기",
        "감정 코칭하기",
        "소통하기",
        "행동 지도하기",
        "스트레스 관리하기",
        "공감적 양육",
        "발달 이해하기"
    )
    val chapterCount = chapterNames.size

    // 양 끝에서 더 이상 넘어가지 않도록 clamp
    fun moveNext() {
        if (chapterIndex < chapterCount - 1) {
            chapterIndex += 1
        }
    }

    fun movePrev() {
        if (chapterIndex > 0) {
            chapterIndex -= 1
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(color = beige)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = topPadding + 20.dp,
                    bottom = bottomPadding + 180.dp
                )
        ) {
            // 카드 헤더
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color(0xFFFFC8A0).copy(alpha = 0.35f),
                        spotColor = Color(0xFFFFC8A0).copy(alpha = 0.35f)
                    )
                    .background(Color(0x5EFFC8A0), RoundedCornerShape(10.dp))
                    .border(
                        width = 2.dp,
                        color = Color(0xFFFFC8A0).copy(alpha = 0.6f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "단원 ${chapterIndex + 1}.",
                        style = IonTheme.typography.body2,
                        color = IonTheme.colors.black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = chapterNames[chapterIndex],
                        style = IonTheme.typography.body2,
                        color = Color(0xFFFF7759),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    // 진행 바 + n / 7
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(6.dp)
                                .background(
                                    Color(0xFFFFE0D5),
                                    RoundedCornerShape(50)
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth((chapterIndex + 1) / chapterCount.toFloat())
                                    .background(
                                        Color(0xFFFF7759),
                                        RoundedCornerShape(50)
                                    )
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "${chapterIndex + 1} / $chapterCount",
                            fontSize = 12.sp,
                            color = Color(0xFF888888)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // 메인 일러스트 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(415.dp)
                    .paint(
                        painterResource(R.drawable.bg_workbook2),
                        contentScale = ContentScale.Fit,
                        alignment = Alignment.Center
                    )
            )
        }

        // 네비게이션 바 바로 위에 고정되는 하단 버튼 영역
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = bottomPadding + 16.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 메인 액션 버튼: 이 단원 시작하기
            Button(
                onClick = {
                    val chapterId = chapterIndex + 1
                    val chapterTitle = chapterTitles[chapterIndex]
                    workbookModel?.updateChapterInfo(chapterId, chapterTitle)
                    navigateToChapter(chapterId)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(7.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF7759),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "이 단원 시작하기",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            // 이전 / 다음 단원 작은 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    30.dp,
                    Alignment.CenterHorizontally
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { movePrev() },
                    enabled = chapterIndex > 0,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFE0D5),
                        contentColor = Color(0xFFFF7759),
                        disabledContainerColor = Color(0xFFFFF0E8),
                        disabledContentColor = Color(0xFFCC9F92)
                    )
                ) {
                    Text(
                        text = "〈 이전 단원",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = { moveNext() },
                    enabled = chapterIndex < chapterCount - 1,  // 🔸 마지막 단원에서 비활성화
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFE0D5),
                        contentColor = Color(0xFFFF7759),
                        disabledContainerColor = Color(0xFFFFF0E8),
                        disabledContentColor = Color(0xFFCC9F92)
                    )
                ) {
                    Text(
                        text = "다음 단원 〉",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Preview(
    name = "Workbook Screen Preview",
    showBackground = true,
    backgroundColor = 0xFFEFE6DC,
    widthDp = 390,
    heightDp = 844
)
@Composable
fun PreviewWorkbookScreen() {
    IonTheme {
        WorkbookScreen(
            navigateToWorkbook = {},
            navigateToChapter = {},
            workbookModel = null,
            topPadding = 0.dp,
            bottomPadding = 56.dp   // 네비 높이 예시
        )
    }
}
