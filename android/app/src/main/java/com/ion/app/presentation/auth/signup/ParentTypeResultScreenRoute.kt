package com.ion.app.presentation.auth.signup

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
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
import kotlin.math.roundToInt


@Preview(
    showBackground = true,
    widthDp = 360,
    heightDp = 780
)
@Composable
fun ParentTypeResultScreenPreview() {
    IonTheme {
        ParentTypeResultScreen(
            result = ParentTypeResultUiState(
                userType = "authoritative",
                userTypeKo = "보호자형",
                description = "아이를 주도적으로 이끌기보다는 도움과 조언을 즐기는 든든한 서포터 타입입니다.",
                mainScores = "보호자형 3점 · 감독자형 3점 · 자유로운형 4점",
                subScores = listOf(
                    "아이를 신체적으로 통제한다: 1점",
                    "자유롭게 양육한다: 1점 ",
                    "이유없이 꾸중한다: 1점 ",
                    "신체적으로 통제한다: 1점",
                    "말로 화를 낸다: 1점",
                    "따뜻하게 지지한다: 1점",
                ),
                isLoading = false,
                errorMessage = null
            ),
            onContinueClick = {}
        )
    }
}

@Composable
fun ParentTypeResultScreenRoute(
    navigateToOnboardingDone: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val activity = LocalContext.current as ComponentActivity
    val viewModel: SignUpViewModel = hiltViewModel(activity)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.userId) {
        val id = uiState.userId
        if (!id.isNullOrBlank()) {
            viewModel.loadParentTypeResult(id)
        }
    }

    ParentTypeResultScreen(
        result = uiState.parentTypeResult,
        onContinueClick = navigateToOnboardingDone
    )
}

@Composable
fun ParentTypeResultScreen(
    result: ParentTypeResultUiState?,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(beige)
            .padding(horizontal = 32.dp)
    ) {
        if (result == null || result.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (result.errorMessage != null) {
            Text(
                text = result.errorMessage ?: "결과를 불러오지 못했어요.",
                color = Color(0xFF412303),
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(50.dp))

                Image(
                    painter = painterResource(R.drawable.img_checkboard),
                    contentDescription = null,
                    modifier = Modifier.size(230.dp)
                )

                Spacer(Modifier.height(32.dp))

                val title = buildAnnotatedString {
                    append("당신은 ")
                    withStyle(
                        SpanStyle(
                            color = IonTheme.colors.orange300,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(result.userTypeKo.ifEmpty { "-" })
                    }
                    append(" 형 부모입니다")
                }

                Text(
                    text = title,
                    fontSize = 19.sp,
                    color = Color(0xFF412303)
                )

                Spacer(Modifier.height(12.dp))

                // 메인 설명 (typeExplain + "입니다.")
                Text(
                    text = result.description.ifEmpty {
                        "아이를 주도적으로 이끌기보다는 도움과 조언을 즐기는 부모님입니다."
                    },
                    fontSize = 15.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFF412303),
                    modifier = Modifier.padding(horizontal = 10.dp),
                    textAlign = TextAlign.Start
                )

                // 세부 항목(서브타입)
                if (result.subScores.isNotEmpty()) {
                    Spacer(Modifier.height(25.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFFF0DB))
                            .padding(vertical = 30.dp, horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            result.subScores.forEachIndexed { index, line ->
                                Text(
                                    text = "       💡️️  "+line,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp,
                                    color = Color(0xFF72655B),
                                    textAlign = TextAlign.Start
                                )
                                if (index != result.subScores.lastIndex) {
                                    Spacer(Modifier.height(2.dp))   // 줄 사이 간격만
                                }
                            }
                        }
                    }
                }


                // 세부 항목
                // 유형 점수 비교 – 서브 정보 박스
                // 유형 점수 비교 – 아래 작은 박스
                if (result.mainScores.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = result.mainScores,
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFF958A7F),
                            textAlign = TextAlign.Center
                        )
                    }
                }


                Spacer(Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(7.dp))
                        .height(44.dp)
                        .noRippleClickable { onContinueClick() }
                        .background(color = IonTheme.colors.orange300),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "회원가입 완료하기",
                        color = IonTheme.colors.white,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

