package com.example.stack.ui.theme

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

val black = Color.Black
val white = Color.White

val DarkColors = darkColors(
    primary = Color.DarkGray,               // should be same as 'surface' below
    primaryVariant = Color.LightGray,
    secondary = white,
    background = black,
    surface = Color.DarkGray,               //<-- alternative: Color.Black, (maybe make this a user setting)
    onPrimary = black,
    onSecondary = black,
    onBackground = white,
    onSurface = white,
)

val LightColors = lightColors(
    primary = white,                        // should be same as 'surface' below
    primaryVariant = Color.LightGray,
    secondary = black,
    background = white,
    surface = white,                        //<-- alternative: Color.LightGray, (maybe make this a user setting)
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

            window.statusBarColor = colors.primary.toArgb() // Sets the status bar color.

            // Check for Build Version and use WindowInsetsController if available
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowInsetsController = window.insetsController
                if (darkTheme) {
                    windowInsetsController?.setSystemBarsAppearance(
                        0, // clear the appearance flag for light status bars
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                } else {
                    windowInsetsController?.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                }
            } else {
                // For older versions, set the status bar color and light status bar appearance
                @Suppress("DEPRECATION")
                if (darkTheme) {
                    window.decorView.systemUiVisibility = 0 // Clear flags
                } else {
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
            }
        }
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        //shapes = Shapes,
        content = content
    )
}
