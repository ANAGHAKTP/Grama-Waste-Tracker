package com.grama.wastetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grama.wastetracker.ui.theme.AccentPrimary
import com.grama.wastetracker.ui.theme.GramaTheme

/**
 * Section header with label, divider line, and optional trailing action.
 * Matches the repeating pattern across all screens:
 *   SERVICES ────────────────────── [Expand]
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailingText: String? = null,
    onTrailingClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            ),
            color = GramaTheme.colors.textTertiary
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(GramaTheme.colors.borderDim)
        )

        if (trailingText != null) {
            Text(
                text = trailingText.uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                ),
                color = AccentPrimary,
                modifier = if (onTrailingClick != null) {
                    Modifier.clickable { onTrailingClick() }
                } else {
                    Modifier
                }
            )
        }
    }
}
