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
    primary = Color.DarkGray, // Primary color for your components like AppBar; matches surface
    primaryVariant = Color.LightGray, // Lighter shade of primary, for surfaces like cards
    secondary = Color.DarkGray, // Color for secondary components, like FABs or interactive elements
    background = Color.Black,   // Background color for large areas of the screen; matches surface
    surface = Color.DarkGray,   // Color for surfaces of components, like cards and menus
    onPrimary = Color.White,    // Text/icon color on top of primary color
    onSecondary = Color.White,  // Text/icon color on top of secondary color
    onBackground = Color.White, // Text/icon color on top of background color
    onSurface = Color.White,    // Text/icon color on top of surface color
)

val LightColors = lightColors(
    primary = Color.White, //""
    primaryVariant = Color.LightGray, // Darker shade of primary, for surfaces like cards
    secondary = Color.White,    //""
    background = Color.White,   //""
    surface = Color.White,      //""
    onPrimary = Color.Black,    //""
    onSecondary = Color.Black,  //""
    onBackground = Color.Black, //""
    onSurface = Color.Black,    //""
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
