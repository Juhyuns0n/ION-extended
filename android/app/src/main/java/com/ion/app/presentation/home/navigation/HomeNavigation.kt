package com.ion.app.presentation.home.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.ion.app.core.navigation.MainTabRoute
import com.ion.app.presentation.home.HomeRoute
import kotlinx.serialization.Serializable

fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    navigate(Home, navOptions)
}

fun NavGraphBuilder.homeGraph(
    navigateToHome: () -> Unit,
    topPadding : Dp,
    bottomPadding: Dp
) {
    composable<Home> {
        HomeRoute(
            modifier = Modifier.fillMaxSize(),
            topPadding = topPadding,
            bottomPadding = bottomPadding
        )
    }
}

@Serializable
data object Home : MainTabRoute


