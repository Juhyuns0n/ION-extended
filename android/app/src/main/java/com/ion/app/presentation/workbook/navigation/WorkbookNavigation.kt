package com.ion.app.presentation.workbook.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ion.app.core.navigation.MainTabRoute
import com.ion.app.presentation.workbook.WorkbookChapterScreen
import com.ion.app.presentation.workbook.WorkbookLessonRoute
import com.ion.app.presentation.workbook.WorkbookRoute
import com.ion.app.presentation.workbook.WorkbookSectionInfoRoute
import kotlinx.serialization.Serializable
import androidx.navigation.navOptions

fun NavController.navigateToWorkbook(navOptions: NavOptions? = null) {
    navigate(
        Workbook,
        navOptions ?: navOptions {
            // 그래프의 시작 지점(탭 루트)까지 팝업
            popUpTo(graph.startDestinationId) {
                inclusive = false
            }
            launchSingleTop = true   // 동일 목적지 중복 방지
        }
    )
}

fun NavController.navigateToChapter(chapterId: Int, navOptions: NavOptions? = null) {
    navigate("chapter/$chapterId", navOptions)
}

fun NavController.navigateToSectionInfo(chapterId: Int, navOptions: NavOptions? = null) {
    navigate("sectionInfo/$chapterId", navOptions)
}

fun NavController.navigateToLesson(
    chapterId: Int,
    lessonId: Int,
    navOptions: NavOptions? = null
) {
    navigate("lesson/$chapterId/$lessonId", navOptions)
}

fun NavGraphBuilder.workbookGraph(
    navigateToWorkbook: () -> Unit,
    navigateToChapter: (Int) -> Unit,
    navigateToLesson: (Int, Int) -> Unit,
    navigateToSectionInfo: (Int) -> Unit,
    topPadding: Dp,
    bottomPadding: Dp
) {
    composable<Workbook> {
        WorkbookRoute(
            modifier = Modifier.fillMaxSize(),
            topPadding = topPadding,
            bottomPadding = bottomPadding,
            navigateToChapter = { chapterId -> navigateToChapter(chapterId) },
            navigateToWorkbook = navigateToWorkbook
        )
    }

    // chapterId를 argument로 받음
    composable(
        route = "chapter/{chapterId}",
        arguments = listOf(navArgument("chapterId") { type = NavType.IntType })
    ) { backStackEntry ->
        val chapterId = backStackEntry.arguments?.getInt("chapterId") ?: 0
        WorkbookChapterScreen(
            chapterId = chapterId,
            navigateToWorkbook = navigateToWorkbook,
            navigateToLesson = { chapterId, lessonId -> navigateToLesson(chapterId, lessonId) },
            navigateToSectionInfo = navigateToSectionInfo,
            topPadding = topPadding
        )
    }

    composable(
        route = "sectionInfo/{chapterId}",
        arguments = listOf(navArgument("chapterId") { type = NavType.IntType })
    ) { backStackEntry ->
        val chapterId = backStackEntry.arguments?.getInt("chapterId") ?: 0

        WorkbookSectionInfoRoute(
            modifier = Modifier.fillMaxSize(),
            bottomPadding = bottomPadding,
            topPadding = topPadding,
            chapterId = chapterId,
            navigateToChapter = navigateToChapter
        )
    }

    composable(
        route = "lesson/{chapterId}/{lessonId}",
        arguments = listOf(
            navArgument("chapterId") { type = NavType.IntType },
            navArgument("lessonId") { type = NavType.LongType }
        )
    ) { backStackEntry ->
        val chapterId = backStackEntry.arguments?.getInt("chapterId") ?: 0
        val lessonId = backStackEntry.arguments?.getLong("lessonId") ?: 0L

        WorkbookLessonRoute(
            modifier = Modifier.fillMaxSize(),
            bottomPadding = bottomPadding,
            navigateToChapter = navigateToChapter,
            navigateToWorkbook = navigateToWorkbook,
            topPadding = topPadding,
            chapterId = chapterId,
            lessonId = lessonId
        )
    }
}

@Serializable
data object Workbook : MainTabRoute
