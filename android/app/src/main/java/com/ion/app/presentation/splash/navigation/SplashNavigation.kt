package com.ion.app.presentation.splash.navigation

import androidx.compose.ui.unit.Dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ion.app.core.navigation.Route
import com.ion.app.presentation.splash.SplashRoute
import kotlinx.serialization.Serializable

fun NavGraphBuilder.splashGraph(
    navigateToSignIn: () -> Unit,
    navigateToUserInfo: () -> Unit,
    bottompadding: Dp
) {
    composable<Splash> {
        SplashRoute(
            navigateToSignIn = navigateToSignIn,
            navigateToUserInfo = navigateToUserInfo,
            padding = bottompadding
        )
    }
}

@Serializable
data object Splash : Route
