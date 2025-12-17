package com.ion.app.presentation.workbook.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

@Composable
fun WorkbookLessonOptionScreen(
    question: String,
    options: List<String>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit,
    isNextEnabled: Boolean,
    nextButtonLabel: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF3C8), RoundedCornerShape(24.dp))
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Q. 타이틀
            Text(
                text = "Q. $question",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF4E4636),
            )

            Spacer(Modifier.height(12.dp))

            // 보기 리스트
            options.forEachIndexed { idx, option ->
                val isSelected = selectedIndex == idx

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) Color(0xCCFFDBBD) else Color.Transparent
                        )
                        .clickable { onSelect(idx) }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) Color(0xFFFF7759)
                                else Color(0xFFFFDBBD)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }

                    Spacer(Modifier.width(10.dp))

                    Text(
                        text = option,
                        fontSize = 14.sp,
                        color = Color(0xFF4E4636),
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onNext,
                enabled = isNextEnabled,
                modifier = Modifier
                    .width(106.dp)
                    .height(43.dp)
                    .align(Alignment.End),
                shape = RoundedCornerShape(7.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF7759),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = nextButtonLabel,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Preview(
    name = "Workbook Option Screen Preview",
    showBackground = true,
    backgroundColor = 0xFFEFE6DC
)
@Composable
fun PreviewWorkbookLessonOptionScreen() {
    WorkbookLessonOptionScreen(
        question = "다음 중 뭐가 맞나요?",
        options = listOf(
            "사과는 빨간색이다.",
            "바나나는 파란색이다.",
            "포도는 초록색이다.",
            "딸기는 까맣다.",
            "수박은 노란색이다."
        ),
        selectedIndex = 1,
        onSelect = {},
        onNext = {},
        isNextEnabled = true,
        nextButtonLabel = "완료 후 피드백"
    )
}
