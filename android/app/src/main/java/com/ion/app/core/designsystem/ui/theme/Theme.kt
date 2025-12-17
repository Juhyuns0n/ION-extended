package com.ion.app.core.designsystem.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LocalIonColors =
    staticCompositionLocalOf<IonColors> { error("No colors provided") }
private val LocalIonTypography =
    staticCompositionLocalOf<IonTypography> { error("No typography provided") }

object IonTheme {
    val colors: IonColors
        @Composable
        @ReadOnlyComposable
        get() = LocalIonColors.current

    val typography: IonTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalIonTypography.current
}

@Composable
fun ProvideIonColorsAndTypography(
    colors: IonColors,
    typography: IonTypography,
    content: @Composable () -> Unit
) {
    val provideColors = remember { colors.copy() }
    provideColors.update(colors)
    val provideTypography = remember { typography.copy() }
    provideTypography.update(typography)

    CompositionLocalProvider(
        LocalIonColors provides provideColors,
        LocalIonTypography provides provideTypography,
        content = content
    )
}

@Composable
fun IonTheme(
    content: @Composable () -> Unit
) {
    val colors = IonDarkColors()
    val typography = IonTypography()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }
    ProvideIonColorsAndTypography(colors, typography) {
        MaterialTheme(
            content = content
        )
    }
}
