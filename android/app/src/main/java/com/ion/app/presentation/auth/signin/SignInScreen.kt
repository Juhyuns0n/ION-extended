package com.ion.app.presentation.auth.signin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import com.ion.app.core.util.noRippleClickable
import com.ion.app.presentation.auth.navigation.SignInViewModel

@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
    IonTheme {
        SignInScreen(
            uiState = SignInUiState(
                email = "test@example.com",
                password = "password123"
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onSignInClick = {},
            onSignUpClick = {},
            modifier = Modifier
        )
    }
}

@Composable
fun SignInScreenRoute(
    navigateToHome: () -> Unit,
    navigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 로그인 성공 시 네비게이션 처리
    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) {
            navigateToHome()
        }
    }

    SignInScreen(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChanged,
        onPasswordChange = viewModel::onPasswordChanged,
        onSignInClick = { viewModel.signIn() },
        onSignUpClick = navigateToSignUp,
        modifier = modifier
    )
}

@Composable
fun SignInScreen(
    uiState: SignInUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit,
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
                .size(240.dp)
                .padding(top = 190.dp)
                .align(Alignment.TopCenter)
        )

        // 입력 폼
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 300.dp, bottom = 180.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SignupInputBox(
                label = "이메일",
                value = uiState.email,
                onValueChange = onEmailChange,
                placeholder = "이메일을 입력해주세요.",
                isPassword = false
            )
            Spacer(Modifier.height(30.dp))

            SignupInputBox(
                label = "비밀번호",
                value = uiState.password,
                onValueChange = onPasswordChange,
                placeholder = "비밀번호를 입력해주세요.",
                isPassword = true
            )

            // 에러 메시지 표시 (있을 때만)
            uiState.errorMessage?.let { error ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = error,
                    color = Color(0xFFB00020),
                    fontSize = 14.sp
                )
            }
        }

        // 하단 버튼 영역
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .padding(bottom =170.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        color = if (uiState.isLoading) {
                            IonTheme.colors.orange300.copy(alpha = 0.6f)
                        } else {
                            IonTheme.colors.orange300
                        }
                    )
                    .noRippleClickable {
                        if (!uiState.isLoading) onSignInClick()
                    }
                    .padding(vertical = 13.dp, horizontal = 50.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (uiState.isLoading) "로그인 중..." else "로그인하기",
                    style = IonTheme.typography.body2,
                    color = IonTheme.colors.white,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = "계정이 없으신가요?",
                style = TextStyle(
                    fontSize = 15.sp,
                    color = IonTheme.colors.white,
                    textDecoration = TextDecoration.Underline
                ),
                modifier = Modifier
                    .noRippleClickable { onSignUpClick() }
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
    isPassword: Boolean = false
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
    }
}
