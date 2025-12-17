package com.ion.app.presentation.auth.signup

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ion.app.R
import com.ion.app.core.designsystem.ui.theme.IonTheme
import com.ion.app.core.designsystem.ui.theme.beige
import com.ion.app.core.util.noRippleClickable
import com.ion.app.presentation.auth.navigation.SignUpViewModel

@Preview(showBackground = true)
@Composable
fun ParentTypeScreenPreview() {
    IonTheme {
        ParentTypeScreen(
            questions = listOf(
                PropensityQuestion(
                    id = 1,
                    question = "아이가 내 마음대로 통제가 안되면 화가 난다",
                    selectedScore = 1       // 첫 번째 원 선택된 상태
                ),
                PropensityQuestion(
                    id = 2,
                    question = "아이가 울거나 떼를 쓸 때 바로 반응해 주려고 한다",
                    selectedScore = 3
                ),
                PropensityQuestion(
                    id = 3,
                    question = "아이에게 규칙을 설명하기보다 지키라고 먼저 말한다",
                    selectedScore = 0       // 아무것도 선택 안 된 상태
                )
            ),
            onScoreSelected = { _, _ -> },
            navigateToSignIn = {},
            onResultClick= {},
            modifier = Modifier
        )
    }
}


@Composable
fun ParentTypeScreenRoute(
    navigateToSignIn: () -> Unit,
    navigateToParentTypeResult: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current as ComponentActivity
    val viewModel: SignUpViewModel = hiltViewModel(activity)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadPropensityTests()
    }

    ParentTypeScreen(
        questions = uiState.propensityQuestions,
        onScoreSelected = { id, score ->
            viewModel.onPropensityScoreChange(id, score)
        },
        navigateToSignIn = navigateToSignIn,
        onResultClick = {
            // 회원가입 POST, 성공 시 결과 화면으로 이동
            viewModel.onParentTestResultClick {
                navigateToParentTypeResult()
            }
        },
        modifier = modifier
    )
}

@Composable
fun ParentTypeScreen(
    questions: List<PropensityQuestion>,
    onScoreSelected: (id: Int, score: Int) -> Unit,
    navigateToSignIn: () -> Unit,
    onResultClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(beige)   // 기존 온보딩 톤 맞춰서 연한 배경
    ) {
        // 상단 로고
        Image(
            painter = painterResource(R.drawable.img_splash_logo),
            contentDescription = null,
            modifier = Modifier
                .size(170.dp)
                .padding(start = 40.dp, top = 60.dp)
                .align(Alignment.TopStart)
        )

        // 질문 리스트
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 170.dp, bottom = 100.dp),
        ) {
            items(questions.size) { index ->
                val question = questions[index]
                PropensityQuestionItem(
                    question = question,
                    onScoreSelected = { score ->
                        onScoreSelected(question.id, score)
                    }
                )
                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        // 하단 버튼 영역
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 35.dp)
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(IonTheme.colors.orange300)
                    .noRippleClickable {
                        onResultClick()
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "결과 보기",
                    style = IonTheme.typography.body2,
                    color = IonTheme.colors.white,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 2.dp, horizontal = 50.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 이미 계정 있을 때
            Text(
                text = "계정이 이미 있으신가요?",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = IonTheme.colors.black,
                    textDecoration = TextDecoration.Underline
                ),
                modifier = Modifier.noRippleClickable { navigateToSignIn() }
            )
        }
    }
}

@Composable
private fun PropensityQuestionItem(
    question: PropensityQuestion,
    onScoreSelected: (Int) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 질문 텍스트
        Text(
            text = question.question,
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4A3424)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 원형 5개 + 양 끝 텍스트
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "그렇다",
                fontSize = 12.sp,
                color = Color(0xFFFF8C54)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                (1..5).forEach { score ->
                    LikertCircle(
                        score = score,
                        isSelected = question.selectedScore == score,
                        onClick = { onScoreSelected(score) }
                    )
                }
            }

            Text(
                text = "그렇지 않다",
                fontSize = 12.sp,
                color = Color(0xFF5C7CFF)
            )
        }
    }
}
@Composable
private fun LikertCircle(
    score: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {

    // --- 색상 변환 로직 ---
    val borderColor = when(score) {
        1 -> Color(0xFFFF8C54)       // 강 주황
        2 -> Color(0xFFFFC2A3)       // 연한 주황
        3 -> Color(0xFFD9D9D9)       // 회색
        4 -> Color(0xFFBBD0FF)       // 연한 파랑
        5 -> Color(0xFF5C7CFF)       // 진한 파랑
        else -> Color.Gray
    }

    val selectedColor = when(score) {
        1 -> Color(0xFFFF8C54)
        2 -> Color(0xFFFFA873)
        3 -> Color(0xFFC4C4C4)
        4 -> Color(0xFF8FB2FF)
        5 -> Color(0xFF5C7CFF)
        else -> Color.Gray
    }

    // --- 크기 규칙 ---
    val outerSize = when(score) {
        1, 5 -> 40.dp
        2, 4 -> 30.dp
        else -> 25.dp
    }

    val innerSize = when(score) {
        1, 5 -> 30.dp
        2, 4 -> 22.dp
        else -> 18.dp
    }

    Box(
        modifier = Modifier
            .size(outerSize)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(
                if (isSelected) selectedColor.copy(alpha = 0.18f)
                else Color.Transparent
            )
            .noRippleClickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(innerSize)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(if (isSelected) selectedColor else Color.Transparent)
                .border(
                    width = 2.dp,
                    color = borderColor,
                    shape = androidx.compose.foundation.shape.CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Image(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White) // 체크 흰색!
                )
            }
        }
    }
}
