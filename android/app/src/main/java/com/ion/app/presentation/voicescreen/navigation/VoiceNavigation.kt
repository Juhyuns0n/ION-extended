package com.ion.app.presentation.voicescreen.navigation

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
import com.ion.app.presentation.voicescreen.VoiceReportRoute
import com.ion.app.presentation.voicescreen.VoiceRoute
import kotlinx.serialization.Serializable

fun NavController.navigateToVoice(navOptions: NavOptions? = null) {
    navigate(Voice, navOptions)
}

fun NavController.navigateToVoiceReport(reportId: Long, navOptions: NavOptions? = null) {
    navigate("voiceReport/$reportId", navOptions)
}

fun NavGraphBuilder.voiceGraph(
    navigateToVoice: () -> Unit,
    navigateToVoiceReport: (Long) -> Unit,
    topPadding: Dp,
    bottomPadding: Dp
) {
    composable<Voice> {
        VoiceRoute(
            modifier = Modifier.fillMaxSize(),
            navigateToVoiceReport = navigateToVoiceReport,
            topPadding = topPadding,
            bottomPadding = bottomPadding
        )
    }

    composable(
        route = "voiceReport/{reportId}",
        arguments = listOf(navArgument("reportId") { type = NavType.LongType })
    ) { backStackEntry ->
        val reportId = backStackEntry.arguments?.getLong("reportId")
        VoiceReportRoute(
            modifier = Modifier.fillMaxSize(),
            topPadding = topPadding,
            bottomPadding = bottomPadding,
            reportId = reportId
        )
    }

}

@Serializable
data object Voice : MainTabRoute
