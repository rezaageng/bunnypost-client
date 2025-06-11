package com.example.bunnypost.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController


private val ColorScheme = darkColorScheme(
    primary = Color(0xFF432dd7),
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF0a0a0a),
)

@Composable
fun BunnyPostTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = ColorScheme
    val systemUiController = rememberSystemUiController()

    SideEffect {
        // Set status bar to transparent color and icons to white
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = false // This makes the status bar icons light (e.g., white)
        )

        // Set navigation bar to transparent and icons to white
        systemUiController.setNavigationBarColor(
            color = Color.Transparent,
            darkIcons = false // This makes the navigation bar icons light (e.g., white)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}