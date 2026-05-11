package com.grama.wastetracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
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
                Image(
                    painter = painterResource(com.grama.wastetracker.R.drawable.ic_logo),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp).padding(bottom = 8.dp)
                )
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

        HorizontalDivider(color = GramaTheme.colors.borderDim, thickness = 1.dp)

        // ── Error Banner ──
        AnimatedVisibility(visible = dashState.error != null) {
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { dashboardViewModel.refresh() },
                color = AccentError.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, AccentError.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.CloudOff, null, tint = AccentError, modifier = Modifier.size(16.dp))
                        Text(dashState.error ?: "", style = MaterialTheme.typography.bodySmall, color = AccentError)
                    }
                    Text("RETRY", style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp, letterSpacing = 1.sp), color = AccentError)
                }
            }
        }

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
            color = if (dashState.activeVehicle != null) AccentPrimary else GramaTheme.colors.bgSecondary,
            border = if (dashState.activeVehicle == null) BorderStroke(1.dp, GramaTheme.colors.borderDim) else null,
            shadowElevation = if (dashState.activeVehicle != null) 12.dp else 0.dp,
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
                            color = (if (dashState.activeVehicle != null) Color.White else GramaTheme.colors.textTertiary).copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (dashState.activeVehicle != null) Icons.Default.Schedule else Icons.Default.PauseCircle,
                                    contentDescription = null,
                                    tint = if (dashState.activeVehicle != null) Color.White else GramaTheme.colors.textTertiary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (dashState.activeVehicle != null) "IN TRANSIT" else "IDLE",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = 9.sp,
                                        letterSpacing = 2.sp,
                                    ),
                                    color = if (dashState.activeVehicle != null) Color.White else GramaTheme.colors.textTertiary
                                )
                            }
                        }
                        Text(
                            text = dashState.activeVehicle?.let { "${it.etaMinutes ?: "--"} Minutes" } ?: "No Active Unit",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Medium
                            ),
                            color = if (dashState.activeVehicle != null) Color.White else GramaTheme.colors.textPrimary
                        )
                        Text(
                            text = dashState.activeVehicle?.let { 
                                if (it.sector.isNotEmpty()) "CURRENT SECTOR • ${it.sector.uppercase()}"
                                else "CURRENT SECTOR • ${it.id.take(8).uppercase()}"
                            } ?: "Fleet currently stationed",
                            style = MaterialTheme.typography.labelMedium.copy(
                                letterSpacing = 2.sp
                            ),
                            color = (if (dashState.activeVehicle != null) Color.White else GramaTheme.colors.textTertiary).copy(alpha = 0.8f)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = (if (dashState.activeVehicle != null) Color.White else GramaTheme.colors.textTertiary).copy(alpha = 0.1f),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (dashState.activeVehicle != null) Icons.Default.Navigation else Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = if (dashState.activeVehicle != null) Color.White else GramaTheme.colors.textTertiary,
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

        Column {
            if (dashState.schedules.isEmpty()) {
                Text(
                    text = "No collection schedules found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GramaTheme.colors.textTertiary,
                    modifier = Modifier.padding(vertical = 32.dp, horizontal = 8.dp)
                )
            } else {
                dashState.schedules.forEachIndexed { index, schedule ->
                    if (index > 0) {
                        HorizontalDivider(color = GramaTheme.colors.borderDim, thickness = 0.5.dp)
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
                                text = (if (schedule.day.isNotEmpty()) schedule.day else schedule.dayOfWeek.take(3)).uppercase(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                ),
                                color = GramaTheme.colors.textTertiary,
                                modifier = Modifier.width(32.dp)
                            )
                            Column {
                                Text(
                                    text = if (schedule.wasteType.isNotEmpty()) schedule.wasteType else schedule.route,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GramaTheme.colors.textPrimary
                                )
                                Text(
                                    text = "${if (schedule.time.isNotEmpty()) schedule.time else schedule.expectedTime} • COLLECTION",
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
}
