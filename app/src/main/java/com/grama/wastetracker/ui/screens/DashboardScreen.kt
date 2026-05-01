package com.grama.wastetracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grama.wastetracker.data.model.UserProfile
import com.grama.wastetracker.ui.components.GeometricCard
import com.grama.wastetracker.ui.components.SectionHeader
import com.grama.wastetracker.ui.theme.*
import com.grama.wastetracker.viewmodel.AuthViewModel
import com.grama.wastetracker.viewmodel.DashboardViewModel

data class QuickAction(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun DashboardScreen(
    profile: UserProfile?,
    authViewModel: AuthViewModel = viewModel(),
    dashboardViewModel: DashboardViewModel = viewModel(),
    onNavigate: (String) -> Unit,
    onSignOut: () -> Unit
) {
    val dashState by dashboardViewModel.state.collectAsState()

    val quickActions = listOf(
        QuickAction("map", "Live Tracking", Icons.Default.LocationOn, AccentPrimary),
        QuickAction("report", "Report Issue", Icons.Default.ErrorOutline, AccentError),
        QuickAction("education", "Waste Guide", Icons.Default.Info, AccentSecondary),
        QuickAction("dashboard", "My Reports", Icons.Default.GridView, GramaTheme.colors.textTertiary),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GramaTheme.colors.bgPrimary)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 48.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // ── Header ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "UNIT IDENTIFIER",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 9.sp,
                        letterSpacing = 2.sp
                    ),
                    color = GramaTheme.colors.textTertiary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = profile?.displayName?.split(" ")?.firstOrNull() ?: "User",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = SpaceGroteskFamily
                    ),
                    color = GramaTheme.colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = AccentPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = profile?.address ?: "Set your location",
                        style = MaterialTheme.typography.bodySmall,
                        color = GramaTheme.colors.textSecondary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Theme toggle
                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { ThemeState.toggle() },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GramaTheme.colors.borderDim),
                    color = Color.Transparent
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (ThemeState.isDarkTheme) Icons.Default.LightMode
                            else Icons.Default.DarkMode,
                            contentDescription = "Toggle theme",
                            tint = GramaTheme.colors.textTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Sign out
                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable {
                            authViewModel.signOut()
                            onSignOut()
                        },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GramaTheme.colors.borderDim),
                    color = Color.Transparent
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Sign out",
                            tint = GramaTheme.colors.textTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Divider(color = GramaTheme.colors.borderDim, thickness = 1.dp)

        // ── AI Daily Insight ──
        AnimatedVisibility(
            visible = dashState.dailyInsight.isNotEmpty() && !dashState.insightLoading,
            enter = fadeIn() + slideInHorizontally { -it }
        ) {
            GeometricCard(
                borderColor = AccentPrimary.copy(alpha = 0.2f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AccentPrimary.copy(alpha = 0.1f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AccentPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "PANCHAYAT INTELLIGENCE",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 9.sp,
                                letterSpacing = 2.sp
                            ),
                            color = AccentPrimary
                        )
                        Text(
                            text = "\"${dashState.dailyInsight}\"",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontStyle = FontStyle.Italic
                            ),
                            color = GramaTheme.colors.textPrimary
                        )
                    }
                }
            }
        }

        // ── Vehicle ETA Card ──
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = AccentPrimary,
            shadowElevation = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "IN TRANSIT",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = 9.sp,
                                        letterSpacing = 2.sp,
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                        Text(
                            text = "12 Minutes",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "MARKET ROAD • SECTOR 04",
                            style = MaterialTheme.typography.labelMedium.copy(
                                letterSpacing = 2.sp
                            ),
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.1f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }

        // ── Quick Actions Grid ──
        SectionHeader(title = "Services")

        // 2x2 grid
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            for (rowIndex in 0..1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    for (colIndex in 0..1) {
                        val index = rowIndex * 2 + colIndex
                        val action = quickActions[index]
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate(action.route) },
                            color = GramaTheme.colors.bgSecondary,
                            border = BorderStroke(0.5.dp, GramaTheme.colors.borderDim),
                            shape = RoundedCornerShape(
                                topStart = if (rowIndex == 0 && colIndex == 0) 12.dp else 0.dp,
                                topEnd = if (rowIndex == 0 && colIndex == 1) 12.dp else 0.dp,
                                bottomStart = if (rowIndex == 1 && colIndex == 0) 12.dp else 0.dp,
                                bottomEnd = if (rowIndex == 1 && colIndex == 1) 12.dp else 0.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Icon(
                                    imageVector = action.icon,
                                    contentDescription = action.label,
                                    tint = action.color,
                                    modifier = Modifier.size(24.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = action.label,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = GramaTheme.colors.textPrimary
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = GramaTheme.colors.textTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Schedule List ──
        SectionHeader(title = "Logistics", trailingText = "Expand")

        val scheduleItems = listOf(
            Triple("MON", "Dry Waste", "08:00"),
            Triple("WED", "Wet Waste", "07:30"),
            Triple("FRI", "Recyclables", "09:00"),
        )

        Column {
            scheduleItems.forEachIndexed { index, (day, type, time) ->
                if (index > 0) {
                    Divider(color = GramaTheme.colors.borderDim, thickness = 0.5.dp)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(vertical = 16.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            ),
                            color = GramaTheme.colors.textTertiary,
                            modifier = Modifier.width(32.dp)
                        )
                        Column {
                            Text(
                                text = type,
                                style = MaterialTheme.typography.titleMedium,
                                color = GramaTheme.colors.textPrimary
                            )
                            Text(
                                text = "$time AM • COLLECTION",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                ),
                                color = GramaTheme.colors.textTertiary
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(GramaTheme.colors.borderDim)
                    )
                }
            }
        }
    }
}
