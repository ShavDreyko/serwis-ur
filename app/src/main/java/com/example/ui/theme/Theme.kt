package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalIsDarkTheme = compositionLocalOf { true }

private val DarkColorScheme = darkColorScheme(
    primary = AmberSafety,
    onPrimary = Slate950,
    primaryContainer = AmberGlow,
    onPrimaryContainer = Slate50,
    secondary = CyanAccent,
    onSecondary = Slate950,
    secondaryContainer = Slate800,
    onSecondaryContainer = CyanAccent,
    tertiary = EmeraldSuccess,
    onTertiary = Slate950,
    error = CrimsonAlert,
    onError = Color.White,
    background = Slate950,
    onBackground = Slate50,
    surface = DarkSurfaceCard,
    onSurface = Color.White,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,
    outline = DarkSurfaceBorder
)

private val LightColorScheme = lightColorScheme(
    primary = AmberGlow,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFEF3C7),
    onPrimaryContainer = Color(0xFF78350F),
    secondary = Color(0xFF0284C7),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = Color(0xFF059669),
    onTertiary = Color.White,
    error = Color(0xFFDC2626),
    onError = Color.White,
    background = Color(0xFFF1F5F9), // Czysty, nowoczesny jasny Slate 100
    onBackground = Color(0xFF0F172A), // Ciemny tekst Slate 900
    surface = Color(0xFFFFFFFF), // Białe tło kart
    onSurface = Color(0xFF0F172A), // Czytelny, kontrastowy tekst główny
    surfaceVariant = Color(0xFFE2E8F0), // Jasnoszary kolor dla chipów i pól
    onSurfaceVariant = Color(0xFF475569), // Slate 600 dla etykiet pomocniczych
    outline = Color(0xFFCBD5E1) // Subtelne obramowania kart
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
