package com.ion.app.presentation.workbook.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ion.app.core.designsystem.ui.theme.IonTheme
import com.ion.app.domain.model.workbook.SimulationLessonModel

@Composable
fun SimulationLessonScreen(
    situation: String,
    dialogues: List<SimulationLessonModel.Dialogue>,
    onSend: (String) -> Unit,
    onFinish: () -> Unit,
    isSending: Boolean,
    isFinished: Boolean
) {
    var input by remember { mutableStateOf("") }

    val hasAnyUserLine = remember(dialogues) {
        dialogues.any { !it.userLine.isNullOrBlank() }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 220.dp, max = 360.dp)
                .background(Color(0xFFFFF3C8), RoundedCornerShape(16.dp))
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 상단 설명 + 대화 로그 (노란 박스 내부만 스크롤)
                Column(
                    modifier = Modifier
                        .weight(1f, fill = true)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = situation,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF333333)
                    )

                    Spacer(Modifier.height(16.dp))

                    dialogues.forEach { d ->
                        d.aiLine?.let { line ->
                            Row {
                                Text(
                                    text = "아이:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF333333)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = line,
                                    fontSize = 14.sp,
                                    color = Color(0xFF333333)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        d.userLine?.let { line ->
                            Row {
                                Text(
                                    text = "나:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFFF81849)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = line,
                                    fontSize = 14.sp,
                                    color = Color(0xFFED6A88)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // 입력 박스
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color.White, RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = input,
                        onValueChange = {
                            if (!isSending && !isFinished) input = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = Color(0xFF333333)
                        ),
                        decorationBox = { inner ->
                            if (input.isEmpty()) {
                                Text(
                                    text = "답변을 입력해주세요",
                                    fontSize = 14.sp,
                                    color = Color(0xFFCCCCCC)
                                )
                            }
                            inner()
                        }
                    )
                }

                Spacer(Modifier.height(12.dp))

                // 하단 두 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onFinish() },
                        enabled = hasAnyUserLine && !isSending && !isFinished,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xCCFFE0D5),
                            contentColor = Color(0xFFFF7759)
                        ),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text(text = "종료하고 피드백보기", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(Modifier.width(3.dp))

                    Button(
                        onClick = {
                            if (input.isNotBlank()) {
                                onSend(input.trim())
                                input = ""
                            }
                        },
                        enabled = !isSending && !isFinished && input.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF8E74),
                            contentColor = Color(0xFFFFFBF4)
                        ),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Text(text = "응답 보내기", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun SimulationLessonScreenPreview() {
    val sampleDialogues = listOf(
        SimulationLessonModel.Dialogue(
            userLine = null,
            aiLine = "엄마, 오늘 유치원에서 친구가 내 장난감을 빌려가서 안 돌려줬어. 그래서 너무 속상했어."
        ),
        SimulationLessonModel.Dialogue(
            userLine = "돌려달라고 했는데 안 돌려줘서 속상했구나.",
            aiLine = null
        ),
        SimulationLessonModel.Dialogue(
            userLine = null,
            aiLine = "응, 그래서 기분이 안 좋아. 엄마라면 어떻게 할 것 같아?"
        )
    )

    IonTheme {
        Column(
            modifier = Modifier
                .background(Color(0xFFFDF2CA))
                .padding(20.dp)
        ) {
            SimulationLessonScreen(
                situation = "지돌이가 친구와 놀다가 감정이 상해 속상해하는 상황입니다.",
                dialogues = sampleDialogues,
                onSend = {},
                onFinish = {},
                isSending = false,
                isFinished = false
            )
        }
    }
}
