package com.example.stack.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val black = Color.Black
val white = Color.White

val DarkColors = darkColors(
    primary = white,
    primaryVariant = Color.LightGray,
    secondary = white,
    background = black,
    surface = Color.DarkGray,
    onPrimary = black,
    onSecondary = black,
    onBackground = white,
    onSurface = white,
)

val LightColors = lightColors(
    primary = black,
    primaryVariant = Color.DarkGray,
    secondary = black,
    background = white,
    surface = white,                        //<-- alternative: Color.LightGray,
    onPrimary = white,
    onSecondary = white,
    onBackground = black,
    onSurface = black,
)

@Composable
fun StackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        content = content
    )
}
