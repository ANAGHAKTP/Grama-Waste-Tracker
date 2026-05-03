package com.grama.wastetracker.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.draw.drawWithContent
import coil3.compose.AsyncImage
import com.grama.wastetracker.data.model.IncidentReport
import com.grama.wastetracker.ui.components.GeometricCard
import com.grama.wastetracker.ui.components.SectionHeader
import com.grama.wastetracker.ui.theme.*
import com.grama.wastetracker.viewmodel.AdminViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AdminDashboardScreen(viewModel: AdminViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    val pendingCount = state.reports.count { it.status == "PENDING" }

    Column(
        modifier = Modifier.fillMaxSize().background(GramaTheme.colors.bgPrimary)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp).padding(top = 48.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Shield, null, tint = AccentPrimary, modifier = Modifier.size(12.dp))
                    Text("PANCHAYAT INTERNAL", style = MaterialTheme.typography.labelLarge.copy(fontSize = 9.sp, letterSpacing = 2.sp), color = AccentPrimary)
                    
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "SEED DATA",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 8.sp, letterSpacing = 1.sp),
                        color = AccentPrimary.copy(alpha = 0.5f),
                        modifier = Modifier.clickable { viewModel.seedData() }
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text("Systems.Terminal", style = MaterialTheme.typography.displaySmall.copy(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold), color = GramaTheme.colors.textPrimary)
            }
            Surface(
                color = GramaTheme.colors.bgTertiary,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GramaTheme.colors.borderDim),
                shadowElevation = 8.dp
            ) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Pulsing Live Dot
                        Box(
                            Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(AccentError)
                        )
                        Text(
                            "LIVE FEED",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 7.sp, letterSpacing = 1.sp),
                            color = AccentError
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "ACTIVE UNITS",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 9.sp, letterSpacing = 2.sp),
                        color = GramaTheme.colors.textTertiary
                    )
                    Text(
                        "0x${state.activeVehicleCount.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                        color = GramaTheme.colors.textPrimary
                    )
                }
            }
        }

        // Error Banner
        AnimatedVisibility(visible = state.error != null) {
            Surface(
                modifier = Modifier.fillMaxWidth().clickable { viewModel.refresh() },
                color = AccentError.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, AccentError.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Warning, null, tint = AccentError, modifier = Modifier.size(16.dp))
                        Text(state.error ?: "", style = MaterialTheme.typography.bodySmall, color = AccentError)
                    }
                    Text("RECONNECT", style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp, letterSpacing = 1.sp), color = AccentError)
                }
            }
        }

        // AI Summary
        SectionHeader(title = "Neural Intelligence", trailingText = if (state.summarizing) "..." else "Generate Summary", onTrailingClick = { viewModel.generateSummary() })
        AnimatedVisibility(visible = state.aiSummary.isNotEmpty()) {
            GeometricCard(borderColor = AccentPrimary.copy(0.2f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Surface(shape = RoundedCornerShape(4.dp), color = AccentPrimary, modifier = Modifier.size(32.dp)) {
                        Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
                    }
                    Column {
                        Text("EXECUTIVE REPORT", style = MaterialTheme.typography.labelLarge.copy(fontSize = 9.sp, letterSpacing = 2.sp), color = AccentPrimary)
                        Spacer(Modifier.height(4.dp))
                        Text("\"${state.aiSummary}\"", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic), color = GramaTheme.colors.textPrimary)
                    }
                }
            }
        }

        // Stats Grid
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(1.dp)) {
            Surface(Modifier.weight(1f), color = GramaTheme.colors.bgSecondary, border = BorderStroke(1.dp, GramaTheme.colors.borderDim), shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text("${state.reports.size}", style = MaterialTheme.typography.displaySmall.copy(fontFamily = SpaceGroteskFamily), color = GramaTheme.colors.textPrimary)
                    Text("TOTAL INTAKE", style = MaterialTheme.typography.labelLarge.copy(fontSize = 9.sp, letterSpacing = 2.sp), color = GramaTheme.colors.textTertiary)
                }
            }
            Surface(Modifier.weight(1f), color = GramaTheme.colors.bgSecondary, border = BorderStroke(1.dp, GramaTheme.colors.borderDim), shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text("$pendingCount", style = MaterialTheme.typography.displaySmall.copy(fontFamily = SpaceGroteskFamily), color = AccentPrimary)
                    Text("UNRESOLVED", style = MaterialTheme.typography.labelLarge.copy(fontSize = 9.sp, letterSpacing = 2.sp), color = GramaTheme.colors.textTertiary)
                }
            }
        }

        // Report Feed
        SectionHeader(title = "Data Feed")
        if (state.loading) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = GramaTheme.colors.textTertiary) }
        } else {
            state.reports.forEach { report -> ReportCard(report = report, onResolve = { viewModel.resolveReport(report.reportId) }) }
        }
    }
}

@Composable
fun ReportCard(
    report: IncidentReport,
    onResolve: () -> Unit
) {
    val isOffender = report.type == "offender"
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .then(
                if (isOffender) Modifier.drawWithContent {
                    drawContent()
                    drawRect(
                        color = AccentError,
                        size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height)
                    )
                } else Modifier
            ),
        color = GramaTheme.colors.bgSecondary,
        shape = RoundedCornerShape(12.dp),
        border = if (isOffender)
            BorderStroke(1.dp, AccentError.copy(alpha = 0.3f))
        else
            BorderStroke(1.dp, GramaTheme.colors.borderDim)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type badge
                Surface(
                    color = if (isOffender) AccentError.copy(alpha = 0.1f)
                            else AccentPrimary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (isOffender) "OFFENDER" else report.issueType.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = if (isOffender) AccentError else AccentPrimary
                    )
                }
                
                // Timestamp
                Text(
                    text = report.timestamp.take(10), // show date portion only
                    style = MaterialTheme.typography.labelSmall,
                    color = GramaTheme.colors.textTertiary
                )
            }
            
            Text(
                text = report.description.ifEmpty { "Null Description" },
                style = MaterialTheme.typography.bodySmall,
                color = GramaTheme.colors.textSecondary,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            
            // Photo thumbnail if present
            report.photoUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = "Report evidence",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
            
            // Resolve button
            Button(
                onClick = onResolve,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isOffender) AccentError else AccentPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "EXECUTE RESOLUTION",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp)
                )
            }
        }
    }
}

private fun formatTimestamp(iso: String): String {
    return try {
        val instant = Instant.parse(iso)
        val formatter = DateTimeFormatter.ofPattern("dd MMM, HH:mm").withZone(ZoneId.of("Asia/Kolkata"))
        formatter.format(instant)
    } catch (e: Exception) { iso }
}
