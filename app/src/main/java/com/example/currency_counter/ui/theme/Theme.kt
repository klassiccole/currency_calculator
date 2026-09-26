package com.example.currency_counter.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = TealGreen,
    onPrimary = DarkGreenInk,
    primaryContainer = LightSage,
    onPrimaryContainer = DarkGreenInk,
    secondary = MediumGreen,
    onSecondary = DarkGreenInk,
    secondaryContainer = PaleSage,
    onSecondaryContainer = DarkGreenInk,
    background = Cream,
    onBackground = InkOnLight,
    surface = Cream,
    onSurface = InkOnLight,
    surfaceVariant = PaleSage,
    onSurfaceVariant = DarkGreenInk
)

private val DarkColorScheme = darkColorScheme(
    primary = MediumGreen,
    onPrimary = DarkGreenInk,
    primaryContainer = DarkGreenInk,
    onPrimaryContainer = LightSage,
    secondary = LightSage,
    onSecondary = DarkGreenInk,
    secondaryContainer = DarkGreenInk,
    onSecondaryContainer = PaleSage,
    background = Color(0xFF121815),
    onBackground = PaleSage,
    surface = Color(0xFF121815),
    onSurface = PaleSage,
    surfaceVariant = DarkGreenInk,
    onSurfaceVariant = PaleSage
)

@Composable
fun CurrencyCalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
