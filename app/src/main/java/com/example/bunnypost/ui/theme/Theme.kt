package com.example.bunnypost.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}