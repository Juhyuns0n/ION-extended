package com.ion.app.presentation.chatbot

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ion.app.R

@Preview(showBackground = true)
@Composable
fun ChatbotScreenPreview() {
    ChatbotScreen(
        topPadding = 0.dp,
        bottomPadding = 30.dp,
        navigateToChat = {}
    )
}

@Composable
fun ChatbotRoute(
    modifier: Modifier = Modifier,
    topPadding: Dp,
    bottomPadding: Dp,
    navigateToChat: () -> Unit,
    navigateToChatHistory: () -> Unit
) {
    ChatbotScreen(
        navigateToChat = navigateToChat,
        navigateToChatHistory = navigateToChatHistory,
        topPadding = 20.dp,
        bottomPadding = bottomPadding
    )
}

@Composable
fun ChatbotScreen(
    navigateToChat: () -> Unit = {},
    navigateToChatHistory: () -> Unit = {},
    topPadding: Dp,
    bottomPadding: Dp
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = topPadding, bottom = bottomPadding)
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_chatbot), // ex) bg_chatbot.jpg
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))
            // 버튼 영역
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ChatbotButton(
                    text = "채팅 시작하기",
                    onClick = { navigateToChat() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                ChatbotButton(
                    text = "지난 채팅 보기",
                    onClick = { navigateToChatHistory() }
                )

                Spacer(Modifier.height(80.dp))
            }
        }
    }
}


@Composable
fun ChatbotButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        Modifier
            .shadow(2.dp, RoundedCornerShape(47.dp), ambientColor = Color(0xFFFFBA8C).copy(alpha = 0.13f))
            .width(220.dp)
            .height(50.dp)
            .background( Color.White, RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = Color(0xFFFFBA8C),
                shape = RoundedCornerShape(47.dp)
            )
            .padding(vertical = 10.dp, horizontal = 14.dp)
            .clickable(
                interactionSource = interaction,
                role = Role.Button,
                onClick = onClick
            )
    ) {
        Text(
            text = text,
            modifier = Modifier.align(Alignment.Center),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

