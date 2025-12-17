package com.ion.app.presentation.splash

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.room.util.copy
import com.ion.app.R
import com.ion.app.core.designsystem.event.LocalSnackBarTrigger
import com.ion.app.core.designsystem.ui.theme.IonTheme
import com.ion.app.core.designsystem.ui.theme.IonTypography
import com.ion.app.core.designsystem.ui.theme.beige
import com.ion.app.core.util.noRippleClickable

@Composable
fun SplashRoute(
    navigateToSignIn: () -> Unit,
    navigateToUserInfo: () -> Unit,
    padding: Dp,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel()
) {

    val context = LocalContext.current
    val showSnackBar = LocalSnackBarTrigger.current

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is SplashStateSideEffect.NavigateToHome -> navigateToSignIn()
                is SplashStateSideEffect.NavigateToUserInfo -> navigateToUserInfo()
                is SplashStateSideEffect.ShowSnackBar -> showSnackBar(sideEffect.message)
                else -> {}
            }
        }
    }

    SplashScreen(
        padding = 120.dp,
        onSignInClick = {
            viewModel.startLogin()
        },
        onSignUpClick = {
            viewModel.startSignUp()
        },
        modifier = modifier
    )
}

@Composable
private fun SplashScreen(
    padding: Dp,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
                            .background(beige)
    ) {
        Image(
            painter = painterResource(R.drawable.bg_splash),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .padding(bottom = padding),
        ) {
            Row(
                modifier = Modifier
                    .width(270.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color = IonTheme.colors.orange300)
                    .noRippleClickable(onClick = onSignInClick)
                    .padding(vertical = 16.dp)
                ,
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "로그인하기",
                    style = IonTheme.typography.body2,
                    color = IonTheme.colors.white,
                    fontSize = 15.sp
                )
            }
            Spacer(Modifier.height(12.dp))

            // 회원가입
            Text(
                text = "아이온이 처음이신가요?",
                style = TextStyle(
                    fontSize = 15.sp,
                    color = IonTheme.colors.gray700,
                    textDecoration = TextDecoration.Underline
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally).noRippleClickable { onSignUpClick() }
            )
        }
    }
}


