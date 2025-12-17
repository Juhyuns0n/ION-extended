package com.ion.app.presentation.main

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.navigation.compose.NavHost
import androidx.navigation.navOptions
import com.ion.app.presentation.auth.navigation.authGraph
import com.ion.app.presentation.chatbot.navigation.chatbotGraph
import com.ion.app.presentation.home.navigation.homeGraph
import com.ion.app.presentation.splash.navigation.splashGraph
import com.ion.app.presentation.voicescreen.navigation.voiceGraph
import com.ion.app.presentation.workbook.navigation.workbookGraph

@Composable
fun MainNavHost(
    navigator: MainNavigator,
    toppadding: Dp,
    bottompadding: Dp,
    modifier: Modifier = Modifier
) {
    val clearStackNavOptions = navOptions {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
        restoreState = true
    }

    val authNavOptions = navOptions {
        launchSingleTop = true
        restoreState = true
    }


    val defaultNavOptions = navOptions {
        launchSingleTop = true
        restoreState = true
    }

    NavHost(
        modifier = modifier,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
        popEnterTransition = { EnterTransition.None },
        popExitTransition = { ExitTransition.None },
        navController = navigator.navController,
        startDestination = navigator.startDestination
    ) {
        splashGraph(
            navigateToSignIn = { navigator.navigateToSignIn(clearStackNavOptions) },
            navigateToUserInfo = { navigator.navigateToUserInfo(defaultNavOptions) },
            bottompadding = bottompadding
        )
        authGraph(
            navigateToNext = { navigator.navigateToParentType(authNavOptions) },
            navigateToSignIn = { navigator.navigateToSignIn(clearStackNavOptions) },
            navigateToUserInfo = { navigator.navigateToUserInfo(authNavOptions) },
            navigateToParentTypeResult = { navigator.navigateToParentTypeResult(authNavOptions) },
            navigateToOnboardingDone = { navigator.navigateToOnboardingDone(authNavOptions) },
            navigateToHome = { navigator.navigateToHome(clearStackNavOptions) }
        )
        homeGraph(
            navigateToHome = { navigator.navigateToHome(clearStackNavOptions)},
            topPadding = toppadding,
            bottomPadding = bottompadding
        )
        voiceGraph(
            navigateToVoice = { navigator.navigateToVoice(defaultNavOptions) },
            navigateToVoiceReport = { reportId ->
                navigator.navigateToVoiceReport(reportId, defaultNavOptions)
            },
            topPadding = toppadding,
            bottomPadding = bottompadding
        )
        workbookGraph(
            // Todo: clearstack말고 따로 정의해서 전 화면으로 돌아가게, 특정화면이면 백핸들러로 따로 관리
            navigateToWorkbook = { navigator.navigateToWorkbook(defaultNavOptions) },
            navigateToChapter = { chapterId -> navigator.navigateToChapter(chapterId, defaultNavOptions) },
            navigateToLesson = { chapterId, lessonId -> navigator.navigateToLesson(chapterId, lessonId, defaultNavOptions)},
            navigateToSectionInfo = { chapterId -> navigator.navigateToSectionInfo(chapterId, defaultNavOptions) },
            topPadding = toppadding,
            bottomPadding = bottompadding
        )

        chatbotGraph(
            navigateToChatbot = { navigator.navigateToChatbot(defaultNavOptions) },
            navigateToChat = { navigator.navigateToChat(defaultNavOptions) },
            navigateToChatHistory = { navigator.navigateToChatHistory(defaultNavOptions) },
            topPadding = toppadding,
            bottomPadding = bottompadding
        )
    }
}
