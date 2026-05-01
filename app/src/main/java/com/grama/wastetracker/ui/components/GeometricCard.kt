package com.grama.wastetracker.ui.components

import androidx.compose.foundation.BorderStroke
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
 * Rounded 12dp corners, surface background, dim border.
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
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor ?: GramaTheme.colors.bgSecondary,
        border = BorderStroke(1.dp, borderColor ?: GramaTheme.colors.borderDim),
        shadowElevation = elevation,
        tonalElevation = 0.dp,
    ) {
        Column(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}
