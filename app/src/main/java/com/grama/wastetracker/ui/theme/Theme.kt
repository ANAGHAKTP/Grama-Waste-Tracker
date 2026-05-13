package com.grama.wastetracker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

// ── Extended colors for the geometric design system ──
@Stable
class GramaColors(
    val bgPrimary: Color,
    val bgSecondary: Color,
    val bgTertiary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val borderDim: Color,
    val accentPrimary: Color = AccentPrimary,
    val accentSecondary: Color = AccentSecondary,
    val accentTertiary: Color = AccentTertiary,
    val accentError: Color = AccentError,
)

val LightGramaColors = GramaColors(
    bgPrimary = LightBgPrimary,
    bgSecondary = LightBgSecondary,
    bgTertiary = LightBgTertiary,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textTertiary = LightTextTertiary,
    borderDim = LightBorderDim,
)

val DarkGramaColors = GramaColors(
    bgPrimary = DarkBgPrimary,
    bgSecondary = DarkBgSecondary,
    bgTertiary = DarkBgTertiary,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textTertiary = DarkTextTertiary,
    borderDim = DarkBorderDim,
)

val LocalGramaColors = staticCompositionLocalOf { LightGramaColors }

// ── Material 3 color schemes ──
private val LightColorScheme = lightColorScheme(
    primary = AccentPrimary,
    onPrimary = Color.White,
    secondary = AccentSecondary,
    tertiary = AccentTertiary,
    error = AccentError,
    background = LightBgPrimary,
    surface = LightBgSecondary,
    surfaceVariant = LightBgTertiary,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorderDim,
    outlineVariant = LightBorderDim,
)

private val DarkColorScheme = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = Color.White,
    secondary = AccentSecondary,
    tertiary = AccentTertiary,
    error = AccentError,
    background = DarkBgPrimary,
    surface = DarkBgSecondary,
    surfaceVariant = DarkBgTertiary,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorderDim,
    outlineVariant = DarkBorderDim,
)

// ── Shapes matching --radius-geometric: 12px ──
val GramaShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),   // geometric radius
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

// ── Theme state manager ──
object ThemeState {
    var isDarkTheme by mutableStateOf(false)
    private var isInitialized = false

    @Composable
    fun Initialize() {
        if (!isInitialized) {
            val systemDark = isSystemInDarkTheme()
            SideEffect {
                isDarkTheme = systemDark
                isInitialized = true
            }
        }
    }

    fun toggle() {
        isDarkTheme = !isDarkTheme
    }
}

@Composable
fun GramaWasteTheme(
    darkTheme: Boolean = ThemeState.isDarkTheme,
    content: @Composable () -> Unit
) {
    ThemeState.Initialize()
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val gramaColors = if (darkTheme) DarkGramaColors else LightGramaColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = gramaColors.bgPrimary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalGramaColors provides gramaColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = GramaTypography,
            shapes = GramaShapes,
            content = content
        )
    }
}

/**
 * Convenience accessor for the extended Grama color palette.
 * Usage: GramaTheme.colors.bgPrimary
 */
object GramaTheme {
    val colors: GramaColors
        @Composable
        get() = LocalGramaColors.current
}
