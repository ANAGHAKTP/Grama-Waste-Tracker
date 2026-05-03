package com.grama.wastetracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grama.wastetracker.ui.theme.AccentPrimary
import com.grama.wastetracker.ui.theme.GramaTheme

data class NavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

val CITIZEN_NAV_ITEMS = listOf(
    NavItem("dashboard", Icons.Default.Home, "HUB"),
    NavItem("map", Icons.Default.Map, "LIVE"),
    NavItem("report", Icons.Default.CameraAlt, "LOG"),
    NavItem("education", Icons.Default.Book, "DOCS"),
)

val ADMIN_NAV_ITEM = NavItem("admin", Icons.Default.Shield, "ROOT")

@Composable
fun BottomNavBar(
    currentRoute: String?,
    isAdmin: Boolean,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = if (isAdmin) CITIZEN_NAV_ITEMS + ADMIN_NAV_ITEM else CITIZEN_NAV_ITEMS

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(12.dp)),
        color = GramaTheme.colors.bgSecondary,
        shadowElevation = 16.dp,
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            GramaTheme.colors.borderDim
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route

                val iconColor by animateColorAsState(
                    targetValue = if (selected) AccentPrimary else GramaTheme.colors.textTertiary,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label = "navIconColor"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onNavigate(item.route) }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = AccentPrimary.copy(alpha = 0.1f),
                                modifier = Modifier
                                    .size(width = 48.dp, height = 32.dp)
                            ) {}
                        }
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = iconColor,
                            modifier = Modifier.size(if (selected) 26.dp else 24.dp)
                        )
                    }

                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = iconColor.copy(alpha = if (selected) 1f else 0.7f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
