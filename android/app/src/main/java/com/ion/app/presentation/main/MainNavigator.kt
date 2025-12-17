package com.ion.app.presentation.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.ion.app.presentation.auth.navigation.navigateToOnboardingDone
import com.ion.app.presentation.auth.navigation.navigateToParentType
import com.ion.app.presentation.auth.navigation.navigateToParentTypeResult
import com.ion.app.presentation.auth.navigation.navigateToSignIn
import com.ion.app.presentation.auth.navigation.navigateToUserInfo
import com.ion.app.presentation.chatbot.navigation.navigateToChat
import com.ion.app.presentation.chatbot.navigation.navigateToChatHistory
import com.ion.app.presentation.chatbot.navigation.navigateToChatbot
import com.ion.app.presentation.home.navigation.navigateToHome
import com.ion.app.presentation.splash.navigation.Splash
import com.ion.app.presentation.voicescreen.navigation.navigateToVoice
import com.ion.app.presentation.voicescreen.navigation.navigateToVoiceReport
import com.ion.app.presentation.workbook.navigation.navigateToChapter
import com.ion.app.presentation.workbook.navigation.navigateToLesson
import com.ion.app.presentation.workbook.navigation.navigateToSectionInfo
import com.ion.app.presentation.workbook.navigation.navigateToWorkbook

class MainNavigator(
    val navController: NavHostController
) {
    private val currentDestination: NavDestination?
        @Composable get() =
            navController
                .currentBackStackEntryAsState().value?.destination

    val startDestination = Splash

    val currentTab: MainNavTab?
        @Composable get() =
            MainNavTab.find { tab ->
                currentDestination?.hasRoute(tab::class) == true
            }

    fun navigate(tab: MainNavTab) {
        val navOptions =
            navOptions {
                navController.currentDestination?.route?.let {
                    popUpTo(it) {
                        inclusive = true
                        saveState = true
                    }
                }
                launchSingleTop = true
                restoreState = true
            }
        when (tab) {
            MainNavTab.VOICE -> navController.navigateToVoice(navOptions)
            MainNavTab.WORKBOOK -> navController.navigateToWorkbook(navOptions)
            MainNavTab.HOME -> navController.navigateToHome(navOptions)
            MainNavTab.CHATBOT -> navController.navigateToChatbot(navOptions)   // Todo
        }
    }

    @Composable
    fun showBottomBar() =
        MainNavTab.contains {
            currentDestination?.hasRoute(it::class) == true
            // TODO: 로그인, 온보딩 시작 단계 추가
                // return currentDestination?.route !in listOf("splash", "login", "onboarding")
        }

    @Composable
    fun showTopBar(): Boolean {
        return currentDestination?.hasRoute(Splash::class) == false
    }

    fun navigateToHome(navOptions: NavOptions) {
        navController.navigateToHome(navOptions)
    }

    fun navigateToWorkbook(navOptions: NavOptions) {
        navController.navigateToWorkbook(navOptions)
    }
    fun navigateToChapter(chapterId: Int, navOptions: NavOptions) {
        navController.navigateToChapter(chapterId,navOptions)
    }
    fun navigateToLesson(chapterId: Int, lessonId: Int, navOptions: NavOptions) {
        navController.navigateToLesson(chapterId, lessonId, navOptions)
    }
    fun navigateToSectionInfo(chapterId: Int, navOptions: NavOptions) {
        navController.navigateToSectionInfo(chapterId, navOptions)
    }

    fun navigateToUserInfo(navOptions: NavOptions) {
        navController.navigateToUserInfo(navOptions)
    }
    fun navigateToParentType(navOptions: NavOptions) {
        navController.navigateToParentType(navOptions)
    }

    fun navigateToParentTypeResult(navOptions: NavOptions) {
        navController.navigateToParentTypeResult(navOptions)
    }

    fun navigateToOnboardingDone(navOptions: NavOptions) {
        navController.navigateToOnboardingDone(navOptions)
    }

    fun navigateToSignIn(navOptions: NavOptions) {
        navController.navigateToSignIn(navOptions)
    }

    fun navigateToVoice(navOptions: NavOptions) {
        navController.navigateToVoice(navOptions)
    }
    fun navigateToVoiceReport(reportId: Long, navOptions: NavOptions) {
        navController.navigateToVoiceReport(reportId, navOptions)
    }

    fun navigateToChatbot(navOptions: NavOptions) {
        navController.navigateToChatbot(navOptions)
    }

    fun navigateToChat(navOptions: NavOptions) {
        navController.navigateToChat(navOptions)
    }

    fun navigateToChatHistory(navOptions: NavOptions) {
        navController.navigateToChatHistory(navOptions)
    }


}

@Composable
fun rememberMainNavigator(
    navController: NavHostController = rememberNavController()
): MainNavigator = remember(navController) {
    MainNavigator(navController)
}

