package com.grama.wastetracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grama.wastetracker.ui.theme.GramaTheme

/**
 * Reusable card component matching the web app's .geometric-card CSS class.
 * Updated with Glassmorphism support for Dark Mode.
 */
@Composable
fun GeometricCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    elevation: Dp = 0.dp,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    // Glassmorphism logic: 0.85 alpha in dark mode, solid in light mode
    val finalBgColor = backgroundColor ?: if (isDark) {
        GramaTheme.colors.bgSecondary.copy(alpha = 0.85f)
    } else {
        GramaTheme.colors.bgSecondary
    }
    
    // White-glow borders in dark mode
    val finalBorderColor = borderColor ?: if (isDark) {
        Color.White.copy(alpha = 0.12f)
    } else {
        GramaTheme.colors.borderDim
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = finalBgColor,
        border = BorderStroke(1.dp, finalBorderColor),
        shadowElevation = elevation,
        tonalElevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}
