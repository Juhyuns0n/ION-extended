package com.ion.app.presentation.workbook.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ion.app.R

@Composable
fun WorkbookLessonDescriptiveScreen(
    question: String,
    answer: String,
    onAnswerChange: (String) -> Unit,
    onNext: () -> Unit,
    isNextEnabled: Boolean,
    nextButtonLabel: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF3C8), RoundedCornerShape(24.dp))
            .padding(horizontal = 20.dp, vertical = 10.dp)
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

            Spacer(Modifier.height(16.dp))

            // 흰색 답변 박스
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                TextField(
                    value = answer,
                    onValueChange = onAnswerChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart),
                    placeholder = {
                        Text(
                            text = "답변을 입력하세요",
                            fontSize = 14.sp,
                            color = Color(0xFFB8B8B8)
                        )
                    },
                    maxLines = Int.MAX_VALUE,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFF4E4636)
                    )
                )
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

//@Preview(
//    name = "Workbook Descriptive Screen Preview",
//    showBackground = true,
//    backgroundColor = 0xFFEFE6DC
//)
//@Composable
//fun PreviewWorkbookLessonDescriptiveScreen() {
//    WorkbookLessonDescriptiveScreen(
//        question = "아이 친구와 갈등이 생겼을 때 어떻게 말하면 좋을까요?",
//        answer = "",
//        onAnswerChange = {},
//        onNext = {}
//    )
//}
