package com.ion.app.presentation.auth.signup

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ion.app.R
import com.ion.app.core.designsystem.ui.theme.IonTheme
import com.ion.app.core.util.noRippleClickable
import com.ion.app.presentation.splash.SplashViewModel

@Composable
@Preview
fun OnboardingDonePreview(){
    IonTheme {
        OnboardingDoneScreen(
            padding = 120.dp,
            onClick = {},
            modifier = Modifier
        )
    }
}

@Composable
fun OnboardingDoneScreenRoute(
    navigateToSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    OnboardingDoneScreen(
        padding = 120.dp,
        onClick = {
            navigateToSignIn()
        },
        modifier = modifier
    )
}

@Composable
private fun OnboardingDoneScreen(
    padding: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(R.drawable.bg_onboardingdone),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp)
                .padding(bottom = 240.dp),
        ) {
            Row(
                modifier = Modifier
                    .width(230.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(color = IonTheme.colors.orange300)
                    .noRippleClickable(onClick = onClick)
                    .padding(vertical = 18.dp)
                ,
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "로그인 화면으로 돌아가기",
                    style = IonTheme.typography.body2,
                    color = IonTheme.colors.white,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
