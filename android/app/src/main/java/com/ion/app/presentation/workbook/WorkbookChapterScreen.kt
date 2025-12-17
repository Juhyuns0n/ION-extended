package com.ion.app.presentation.workbook

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ion.app.R
import com.ion.app.core.designsystem.ui.theme.IonTheme
import com.ion.app.core.designsystem.ui.theme.beige
import com.ion.app.presentation.workbook.navigation.WorkbookViewModel

@Composable
fun WorkbookChapterScreen(
    chapterId: Int,
    navigateToWorkbook: () -> Unit = {},
    navigateToLesson: (Int, Int) -> Unit = { _, _ -> },
    navigateToSectionInfo: (Int) -> Unit = {},
    workbookModel: WorkbookViewModel = hiltViewModel(),
    topPadding: Dp
) {
    BackHandler {
        navigateToWorkbook()
    }

    val uiState by workbookModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(chapterId) {
        workbookModel.updateChapterInfo(chapterId, "단원 $chapterId")
    }

    // 🔹 chapterTitle 분리 ("감정 코칭: 감정 인식, 표현, 수용" → main / sub)
    val rawTitle = uiState.chapterName.orEmpty()
    val titleParts = rawTitle.split(":", limit = 2)
    val mainTitle = titleParts.getOrNull(0)?.trim().orEmpty()
    val subTitle = titleParts.getOrNull(1)?.trim().orEmpty()

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
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.width(100.dp)) {
                    Text(
                        text = "단원 ${uiState.chapterId ?: "-"}",
                        style = IonTheme.typography.body2,
                        color = IonTheme.colors.black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }

                // lesson 완료 비율로 progress 계산
                val completedCount = uiState.lessonList.count { it.progressStatus == 1 }
                val totalCount = uiState.lessonList.size
                val progress =
                    if (totalCount > 0) completedCount / totalCount.toFloat() else 0f

                ProgressBar(progress = progress)
            }

            Spacer(modifier = Modifier.height(50.dp))

            // 🔹 Lesson info card (프리뷰와 동일 스타일)
            Box(
                modifier = Modifier
                    .shadow(
                        2.dp,
                        RoundedCornerShape(16.dp),
                        ambientColor = Color(0xFF772A0B).copy(alpha = 0.13f)
                    )
                    .fillMaxWidth()
                    .height(106.dp)
                    .background(Color(0xFFFFACD7), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = mainTitle,
                            color = Color(0xF0000000),
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                        )
                        if (subTitle.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = subTitle,
                                color = Color(0xF0000000),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Button(
                        onClick = { navigateToSectionInfo(chapterId) },
                        modifier = Modifier.fillMaxHeight(),
                        shape = RoundedCornerShape(0.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color.Unspecified
                        )
                    ) {
                        Image(
                            painter = painterResource(R.drawable.img_button_section),
                            contentDescription = null,
                            modifier = Modifier.fillMaxHeight(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            Spacer(Modifier.height(80.dp))

            uiState.lessonList.forEachIndexed { index, lesson ->
                LessonCard(
                    lessonNumber = index + 1,
                    subtitle = lesson.lessonTitle,
                    isCompleted = lesson.progressStatus == 1,
                    onClick = {
                        navigateToLesson(chapterId, lesson.lessonId)
                    }
                )

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun ProgressBar(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .height(10.dp)
            .paint(
                painterResource(R.drawable.img_progressbar),
                contentScale = ContentScale.FillBounds
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .paint(
                    painterResource(R.drawable.img_filledprogressbar),
                    contentScale = ContentScale.FillBounds
                )
        )
    }
}

@Composable
fun LessonCard(
    lessonNumber: Int,
    subtitle: String,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .shadow(
                2.dp,
                RoundedCornerShape(16.dp),
                ambientColor = Color(0xFF772A0B).copy(alpha = 0.13f)
            )
            .fillMaxWidth()
            .heightIn(min = 106.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFF3C8),
            contentColor = Color.Unspecified
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 0.dp)
        ) {
            // 텍스트 영역
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lesson $lessonNumber",
                        color = Color(0xF0000000),
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                    )

                    if (isCompleted) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    Color(0xFF4CAF50).copy(alpha = 0.15f),
                                    RoundedCornerShape(50)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "완료",
                                color = Color(0xFF2E7D32),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(3.dp))

                Text(
                    text = subtitle,
                    color = Color(0xF0000000),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 26.sp,
                )
            }

            // 우측 일러스트 (완료/미완료에 따라 변경)
            val illustrationRes = if (isCompleted) {
                R.drawable.img_lesson_unfinished   // 완료용
            } else {
                R.drawable.img_lesson_finished // 미완료용
            }

            Image(
                painter = painterResource(illustrationRes),
                contentDescription = null,
                modifier = Modifier.size(85.dp)
            )
        }
    }
}

@Preview(
    name = "Workbook Chapter Screen Preview",
    showBackground = true,
    backgroundColor = 0xFFEFE6DC,
    widthDp = 390,
    heightDp = 844
)
@Composable
fun PreviewWorkbookChapterScreen() {
    IonTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(color = beige)
                .padding(top = 0.dp)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.width(100.dp)) {
                        Text(
                            text = "단원 2",
                            style = IonTheme.typography.body2,
                            color = IonTheme.colors.black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                    }
                    ProgressBar(progress = 2f / 7f)
                }

                Spacer(modifier = Modifier.height(50.dp))

                // preview 용 chapterTitle
                val previewTitle = "감정 코칭: 감정 인식, 표현, 수용"
                val parts = previewTitle.split(":", limit = 2)
                val mainTitle = parts[0].trim()
                val subTitle = parts.getOrNull(1)?.trim().orEmpty()

                Box(
                    modifier = Modifier
                        .shadow(
                            2.dp,
                            RoundedCornerShape(16.dp),
                            ambientColor = Color(0xFF772A0B).copy(alpha = 0.13f)
                        )
                        .fillMaxWidth()
                        .height(106.dp)
                        .background(Color(0xFFFFACD7), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = mainTitle,
                                color = Color(0xF0000000),
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = subTitle,
                                color = Color(0xF0000000),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Button(
                            onClick = { },
                            modifier = Modifier.fillMaxHeight(),
                            shape = RoundedCornerShape(0.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.Unspecified
                            )
                        ) {
                            Image(
                                painter = painterResource(R.drawable.img_button_section),
                                contentDescription = null,
                                modifier = Modifier.fillMaxHeight(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }

                Spacer(Modifier.height(80.dp))

                LessonCard(
                    lessonNumber = 1,
                    subtitle = "나의 감정을 이해하기",
                    isCompleted = true,
                    onClick = {}
                )
                Spacer(modifier = Modifier.height(30.dp))
                LessonCard(
                    lessonNumber = 2,
                    subtitle = "감정 신호 알아보기",
                    isCompleted = false,
                    onClick = {}
                )
                Spacer(modifier = Modifier.height(30.dp))
                LessonCard(
                    lessonNumber = 3,
                    subtitle = "감정과 행동 연결해보기",
                    isCompleted = false,
                    onClick = {}
                )
            }
        }
    }
}
