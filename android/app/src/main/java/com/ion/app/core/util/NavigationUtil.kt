package com.ion.app.core.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlin.reflect.KClass

@Composable
fun rememberParentEntryOrNull(
    navController: NavHostController,
    parentRoute: KClass<out Any>
): NavBackStackEntry? {
    val parentEntryState = remember { mutableStateOf<NavBackStackEntry?>(null) }

    LaunchedEffect(navController) {
        try {
            parentEntryState.value = navController.getBackStackEntry(parentRoute.simpleName!!)
        } catch (_: IllegalArgumentException) {
            // 부모가 아직 스택에 없으면 무시
        }
    }

    return parentEntryState.value
}