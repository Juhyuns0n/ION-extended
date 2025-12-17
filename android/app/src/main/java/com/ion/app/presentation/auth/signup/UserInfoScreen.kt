package com.ion.app.presentation.auth.signup

import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
fun UserInfoScreenPreview() {
    IonTheme {
        UserInfoScreen(
            uiState = SignUpUiState(
                step = 5,
                parentName = "d",
                parentNickname = "ddd",
                kidsNickname = "ddd",
                kidsAge = "dddd"
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onPasswordCheckChange = {},
            onParentNameChange = {},
            onParentNicknameChange = {},
            onKidsNicknameChange = {},
            onKidsAgeChange = {},
            onKidsTendencyChange = {},
            onKidsNoteChange = {},
            onGoalChange = {},
            onWorryChange = {},
            onPersonalInformationAgreeChange = {},
            onNextClick = {},
            onSignInClick = {},
            modifier = Modifier
        )
    }
}

@Composable
fun UserInfoScreenRoute(
    navigateToNext: () -> Unit,
    navigateToSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activity = LocalContext.current as ComponentActivity
    val viewModel: SignUpViewModel = hiltViewModel(activity)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    BackHandler(enabled = uiState.step > 1) {
        viewModel.onPrevClicked()
    }

    // 약관 에러
    LaunchedEffect(uiState.termsError) {
        uiState.termsError?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearTermsError()
        }
    }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            android.widget.Toast
                .makeText(context, msg, android.widget.Toast.LENGTH_SHORT)
                .show()
            viewModel.clearToastMessage()
        }
    }

    UserInfoScreen(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onPasswordCheckChange = viewModel::onPasswordCheckChange,
        onParentNameChange = viewModel::onParentNameChange,
        onParentNicknameChange = viewModel::onParentNicknameChange,
        onKidsNicknameChange = viewModel::onKidsNicknameChange,
        onKidsAgeChange = viewModel::onKidsAgeChange,
        onKidsTendencyChange = viewModel::onKidsTendencyChange,
        onKidsNoteChange = viewModel::onKidsNoteChange,
        onGoalChange = viewModel::onGoalChange,
        onWorryChange = viewModel::onWorryChange,
        onPersonalInformationAgreeChange = viewModel::onPersonalInformationAgreeChange,
        onNextClick = { viewModel.onNextClicked { navigateToNext() } },
        onSignInClick = navigateToSignIn,
        modifier = modifier
    )
}


@Composable
fun UserInfoScreen(
    uiState: SignUpUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordCheckChange: (String) -> Unit,
    onParentNameChange: (String) -> Unit,
    onParentNicknameChange: (String) -> Unit,
    onKidsNicknameChange: (String) -> Unit,
    onKidsAgeChange: (String) -> Unit,
    onKidsTendencyChange: (String) -> Unit,
    onKidsNoteChange: (String) -> Unit,
    onGoalChange: (String) -> Unit,
    onWorryChange: (String) -> Unit,
    onPersonalInformationAgreeChange: (Boolean) -> Unit,
    onNextClick: () -> Unit,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFCFA8))
    ) {
        Image(
            painter = painterResource(R.drawable.img_splash_logo),
            contentDescription = null,
            modifier = Modifier
                .size(170.dp)
                .padding(start = 40.dp, top = 60.dp)
        )

        // 입력 폼 (위쪽)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 180.dp, bottom = 180.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (uiState.step) {
                1 -> {
                    // -------- 1단계: 계정 + 부모 정보 --------
                    SignupInputBox(
                        label = "이메일",
                        value = uiState.email,
                        onValueChange = onEmailChange,
                        placeholder = "이메일을 입력해주세요.",
                        isPassword = false
                    )
                    Spacer(Modifier.height(20.dp))

                    SignupInputBox(
                        label = "비밀번호",
                        value = uiState.password,
                        onValueChange = onPasswordChange,
                        placeholder = "비밀번호를 입력해주세요.",
                        isPassword = true,
                        errorText = uiState.passwordError
                    )
                    Spacer(Modifier.height(20.dp))

                    SignupInputBox(
                        label = "비밀번호 확인",
                        value = uiState.passwordCheck,
                        onValueChange = onPasswordCheckChange,
                        placeholder = "비밀번호를 다시 입력해주세요.",
                        isPassword = true,
                        errorText = uiState.passwordCheckError
                    )
                    Spacer(Modifier.height(20.dp))

                    SignupInputBox(
                        label = "부모 이름",
                        value = uiState.parentName,
                        onValueChange = onParentNameChange,
                        placeholder = "부모 이름을 입력해주세요.",
                        isPassword = false
                    )
                    Spacer(Modifier.height(20.dp))

                    SignupInputBox(
                        label = "부모 닉네임 설정",
                        value = uiState.parentNickname,
                        onValueChange = onParentNicknameChange,
                        placeholder = "부모 닉네임을 입력해주세요.",
                        isPassword = false
                    )
                }

                2 -> {
                    // -------- 2단계: 아이 정보 --------
                    SignupInputBox(
                        label = "아이 닉네임",
                        value = uiState.kidsNickname,
                        onValueChange = onKidsNicknameChange,
                        placeholder = "아이 닉네임을 입력해주세요.",
                        isPassword = false
                    )
                    Spacer(Modifier.height(20.dp))

                    SignupInputBox(
                        label = "아이 나이",
                        value = uiState.kidsAge,
                        onValueChange = onKidsAgeChange,
                        placeholder = "나이를 입력해주세요.",
                        isPassword = false
                    )
                    Spacer(Modifier.height(20.dp))

                    SignupInputBox(
                        label = "아이 성향",
                        value = uiState.kidsTendency,
                        onValueChange = onKidsTendencyChange,
                        placeholder = "예: 활발함, 차분함 등",
                        isPassword = false
                    )
                    Spacer(Modifier.height(20.dp))

                    SignupInputBox(
                        label = "아이 정보",
                        value = uiState.kidsNote,
                        onValueChange = onKidsNoteChange,
                        placeholder = "예: 땅콩 알러지 있음",
                        isPassword = false
                    )
                }

                3 -> {
                    // -------- 3단계: 목표, 걱정, 약관 동의 --------

                    // 목표 (멀티라인, 큰 칸)
                    SignupMultiLineBox(
                        label = "육아 목표",
                        value = uiState.goal,
                        onValueChange = onGoalChange,
                        placeholder = "예: 아이가 감정을 솔직하게 표현할 수 있도록 돕고 싶어요."
                    )
                    Spacer(Modifier.height(24.dp))

                    // 주요 걱정 (멀티라인, 큰 칸)
                    SignupMultiLineBox(
                        label = "요즘 가장 걱정되는 점",
                        value = uiState.worry,
                        onValueChange = onWorryChange,
                        placeholder = "예: 아이가 잠들기 전마다 떼를 쓰고 울어서 걱정돼요."
                    )
                    Spacer(Modifier.height(44.dp))

                    // 약관 동의 섹션
                    TermsAgreeSection(
                        checked = uiState.personalInformationAgree,
                        onCheckedChange = onPersonalInformationAgreeChange
                    )
                }

                4 -> SignUpSummaryContent(uiState)

                5-> {
                    Image(
                        painter = painterResource(R.drawable.bg_parent_type),
                        contentDescription = null,
                        modifier = Modifier
                            .size(300.dp)
                    )

                    Image(
                        painter = painterResource(R.drawable.bg_parent_type_info),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                    )

                    Spacer(Modifier.height(24.dp))
                }
            }
        }

        // 하단 버튼 영역 (맨 위에 깔려서 항상 클릭 가능)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .padding(bottom = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val nextButtonText = when (uiState.step) {
                1, 2 -> "다음"
                3 -> "다음 단계로: 회원정보 검토하기"
                4 -> "저장"
                5 -> "유형검사 시작하기"
                else -> "다음"
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .background(color = IonTheme.colors.orange300)
                    .noRippleClickable {
                        Log.d("SignUp", "Next clicked, step=${uiState.step}")
                        onNextClick()
                    }
                    .padding(vertical = 13.dp, horizontal = 50.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = nextButtonText,
                    style = IonTheme.typography.body2,
                    color = IonTheme.colors.white,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "계정이 이미 있으신가요?",
                style = TextStyle(
                    fontSize = 15.sp,
                    color = IonTheme.colors.white,
                    textDecoration = TextDecoration.Underline
                ),
                modifier = Modifier
                    .noRippleClickable { onSignInClick() }
            )
        }

    }
}

@Composable
fun SignupInputBox(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean,
    errorText: String? = null
) {
    Column(
        modifier = Modifier.padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A3424)
            )
        )

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    2.dp,
                    RoundedCornerShape(17.dp),
                    ambientColor = Color(0xFFFFBA8C).copy(alpha = 0.13f)
                )
                .height(50.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(Color.White),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    color = Color(0xFF333333)
                ),
                visualTransformation = if (isPassword) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                keyboardOptions = if (isPassword) {
                    KeyboardOptions(keyboardType = KeyboardType.Password)
                } else {
                    KeyboardOptions.Default
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color(0xFFD0CBC5),
                            fontSize = 15.sp
                        )
                    }
                    inner()
                }
            )
        }

        if (!errorText.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = errorText,
                color = Color(0xFFE53935),
                fontSize = 12.sp
            )
        }
    }
}
@Composable
fun SignupMultiLineBox(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column(
        modifier = Modifier.padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A3424)
            )
        )

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    2.dp,
                    RoundedCornerShape(10.dp),
                    ambientColor = Color(0xFFFFBA8C).copy(alpha = 0.13f)
                )
                .height(80.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White),
            contentAlignment = Alignment.TopStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = false,
                maxLines = 4,
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    color = Color(0xFF333333)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color(0xFFD0CBC5),
                            fontSize = 14.sp
                        )
                    }
                    inner()
                }
            )
        }
    }
}

@Composable
fun TermsAgreeSection(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "개인정보 수집 및 이용 동의",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A3424)
            )
        )

        Spacer(Modifier.height(15.dp))

        // 약관 내용 박스
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 1.dp,
                    shape = RoundedCornerShape(4.dp),
                    ambientColor = Color(0xFFFFBA8C).copy(alpha = 0.15f)
                )
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFFFF6ED))
                .padding(22.dp)
        ) {
            Text(
                text = "입력하신 정보는 맞춤형 서비스를 위해 안전하게 암호화되어 사용됩니다. 외부 공유는 이루어지지 않으며, 언제든 삭제를 요청하실 수 있습니다.",
                fontSize = 15.sp,
                color = Color(0xFF4A3424),
                lineHeight = 22.sp
            )
        }

        Spacer(Modifier.height(5.dp))

        // 체크박스 + 문구
        androidx.compose.foundation.layout.Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFFFF7759)
                )
            )
            Text(
                text = "개인정보 수집·이용에 동의합니다.",
                fontSize = 14.sp,
                color = Color(0xFF4A3424)
            )
        }
    }
}

@Composable
fun SignUpSummaryContent(
    uiState: SignUpUiState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SummarySection(title = "계정 정보") {
            SummaryItem("이메일", uiState.email)
        }

        SummarySection(title = "부모 정보") {
            SummaryItem("부모 이름", uiState.parentName)
            SummaryItem("부모 닉네임", uiState.parentNickname)
        }

        SummarySection(title = "아이 정보") {
            SummaryItem("아이 닉네임", uiState.kidsNickname)
            SummaryItem("아이 나이", uiState.kidsAge)
            SummaryItem("아이 성향", uiState.kidsTendency)
            SummaryItem("아이 정보", uiState.kidsNote)
        }

        SummarySection(title = "육아 목표 & 걱정") {
            SummaryItem("육아 목표", uiState.goal)
            SummaryItem("요즘 걱정", uiState.worry)
        }
    }
}

@Composable
private fun SummarySection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 1.dp,
                shape = RoundedCornerShape(10.dp),
                ambientColor = IonTheme.colors.primary300Alpha10
            )
            .clip(RoundedCornerShape(10.dp))
            .background(beige)   // 살짝 베이지 톤 카드
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column {
            SectionTitle(title)
            Spacer(Modifier.height(4.dp))
            content()
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = IonTheme.colors.brown700,
    )
}

@Composable
private fun SummaryItem(label: String, value: String) {
    if (value.isEmpty()) return

    Column(
        modifier = Modifier.padding(vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = IonTheme.colors.brown500
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = IonTheme.colors.brown700
        )
    }
}


