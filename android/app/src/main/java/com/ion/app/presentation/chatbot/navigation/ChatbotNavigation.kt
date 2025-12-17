package com.ion.app.presentation.chatbot.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.ion.app.core.navigation.MainTabRoute
import com.ion.app.presentation.chatbot.ChatHistoryRoute
import com.ion.app.presentation.chatbot.ChatRoute
import com.ion.app.presentation.chatbot.ChatbotRoute
import kotlinx.serialization.Serializable

fun NavController.navigateToChatbot(navOptions: NavOptions? = null) {
    navigate(Chatbot, navOptions)
}
fun NavController.navigateToChat(navOptions: NavOptions? = null) {
    navigate(Chat, navOptions)
}
//TODO: 스크린 생성 후 수정
fun NavController.navigateToChatHistory(navOptions: NavOptions? = null) {
    navigate(ChatHistory, navOptions)
}

fun NavGraphBuilder.chatbotGraph(
    navigateToChatbot: () -> Unit,
    navigateToChat: () -> Unit,
    navigateToChatHistory: () -> Unit,
    topPadding: Dp,
    bottomPadding: Dp
) {
    composable<Chatbot> {
        ChatbotRoute(
            modifier = Modifier.fillMaxSize(),
            topPadding = topPadding,
            bottomPadding = bottomPadding,
            navigateToChat = navigateToChat,
            navigateToChatHistory = navigateToChatHistory
        )
    }
    composable<Chat> {
        ChatRoute(
            topPadding = topPadding,
            bottomPadding = bottomPadding
        )
    }
    composable<ChatHistory> {
        ChatHistoryRoute(
            topPadding = topPadding,
            bottomPadding = bottomPadding
        )
    }
}

@Serializable
data object Chatbot : MainTabRoute
@Serializable
data object Chat : MainTabRoute
@Serializable
data object ChatHistory : MainTabRoute