package com.grama.wastetracker.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grama.wastetracker.ui.components.GeometricCard
import com.grama.wastetracker.ui.components.SectionHeader
import com.grama.wastetracker.ui.theme.*
import com.grama.wastetracker.viewmodel.EducationViewModel

data class WasteGuideline(
    val category: String,
    val icon: ImageVector,
    val items: List<String>,
    val description: String
)

val GUIDELINES = listOf(
    WasteGuideline("Wet Waste", Icons.Default.Eco, listOf("Fruit peels", "Vegetable scraps", "Leftover food", "Used tea bags"), "Organic waste that can be composted. Always use green bins."),
    WasteGuideline("Dry Waste", Icons.Default.Bolt, listOf("Plastics", "Paper", "Cardboard", "Glass bottles", "Metal cans"), "Non-biodegradable waste. Ensure items are dry before disposal."),
    WasteGuideline("Hazardous", Icons.Default.Warning, listOf("Used batteries", "Tablets/Medicine", "Paints", "Cleaning chemicals"), "Requires special handling. Wrap securely and keep separate."),
    WasteGuideline("Sanitary", Icons.Default.Cancel, listOf("Used diapers", "Sanitary napkins", "Medical waste"), "Should be wrapped in paper and marked with a red dot."),
)

@Composable
fun EducationScreen(viewModel: EducationViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().background(GramaTheme.colors.bgPrimary)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp).padding(top = 48.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Column {
            Text("Docs.Protocol", style = MaterialTheme.typography.displaySmall.copy(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold), color = GramaTheme.colors.textPrimary)
            Spacer(Modifier.height(8.dp))
            Text("PUBLIC SEGREGATION STANDARDS", style = MaterialTheme.typography.labelLarge.copy(fontSize = 9.sp, letterSpacing = 2.sp), color = GramaTheme.colors.textTertiary)
        }
        Divider(color = GramaTheme.colors.borderDim, thickness = 1.dp)

        // AI Search
        SectionHeader(title = "Neural Assistant")
        GeometricCard {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.query, onValueChange = viewModel::updateQuery,
                    placeholder = { Text("DISPOSAL QUERY...", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)) },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.Search, null, Modifier.size(16.dp)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPrimary, unfocusedBorderColor = GramaTheme.colors.borderDim, focusedContainerColor = GramaTheme.colors.bgPrimary, unfocusedContainerColor = GramaTheme.colors.bgPrimary),
                    shape = RoundedCornerShape(4.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                )
                Button(onClick = viewModel::searchWasteClassification, enabled = !state.searching && state.query.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary, disabledContainerColor = GramaTheme.colors.bgTertiary),
                    shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(16.dp)) {
                    if (state.searching) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                    else Icon(Icons.Default.AutoAwesome, null, Modifier.size(20.dp))
                }
            }
            AnimatedVisibility(visible = state.aiResult != null) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    Divider(color = GramaTheme.colors.borderDim)
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                        Surface(shape = RoundedCornerShape(4.dp), color = AccentPrimary.copy(0.1f), modifier = Modifier.size(32.dp)) {
                            Box(Alignment.Center) { Icon(Icons.Default.AutoAwesome, null, tint = AccentPrimary, modifier = Modifier.size(16.dp)) }
                        }
                        Column {
                            Text("AI RECOMMENDATION", style = MaterialTheme.typography.labelLarge.copy(fontSize = 9.sp, letterSpacing = 2.sp), color = GramaTheme.colors.textTertiary)
                            Spacer(Modifier.height(4.dp))
                            Row {
                                Text("[${state.aiResult?.category}]", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = AccentPrimary)
                                Spacer(Modifier.width(8.dp))
                                Text(state.aiResult?.instruction ?: "", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = GramaTheme.colors.textPrimary)
                            }
                        }
                    }
                }
            }
        }

        // Category Cards
        GUIDELINES.forEach { guide ->
            val expanded = state.expandedCategory == guide.category
            Surface(color = GramaTheme.colors.bgSecondary, border = BorderStroke(1.dp, if (expanded) AccentPrimary.copy(0.3f) else GramaTheme.colors.borderDim), shape = RoundedCornerShape(12.dp)) {
                Column {
                    Row(Modifier.fillMaxWidth().clickable { viewModel.toggleCategory(guide.category) }.padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(Modifier.size(48.dp), border = BorderStroke(1.dp, if (expanded) AccentPrimary else GramaTheme.colors.borderDim), color = GramaTheme.colors.bgPrimary, shape = RoundedCornerShape(4.dp)) {
                                Box(Alignment.Center) { Icon(guide.icon, null, tint = if (expanded) AccentPrimary else GramaTheme.colors.textTertiary, modifier = Modifier.size(24.dp)) }
                            }
                            Column {
                                Text(guide.category.uppercase(), style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 2.sp), color = GramaTheme.colors.textPrimary)
                                Text(guide.description, style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.sp, fontFamily = FontFamily.Monospace), color = GramaTheme.colors.textTertiary, maxLines = 1)
                            }
                        }
                        Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = GramaTheme.colors.textTertiary, modifier = Modifier.size(16.dp))
                    }
                    AnimatedVisibility(visible = expanded) {
                        Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp)) {
                            Divider(color = GramaTheme.colors.borderDim)
                            Spacer(Modifier.height(16.dp))
                            guide.items.chunked(2).forEach { row ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    row.forEach { item ->
                                        Row(Modifier.weight(1f).padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, null, tint = AccentTertiary, modifier = Modifier.size(12.dp))
                                            Text(item.uppercase(), style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp, letterSpacing = 0.sp), color = GramaTheme.colors.textSecondary)
                                        }
                                    }
                                    if (row.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Rewards Card
        Surface(color = GramaTheme.colors.bgTertiary, border = BorderStroke(1.dp, GramaTheme.colors.borderDim), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(24.dp)) {
                Text("Incentive Tier 02", style = MaterialTheme.typography.headlineMedium.copy(fontFamily = SpaceGroteskFamily), color = GramaTheme.colors.textPrimary)
                Spacer(Modifier.height(8.dp))
                Text("MAINTAIN PROTOCOL COMPLIANCE TO AGGREGATE VILLAGE FAIR VALIDATION CREDITS.", style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp, letterSpacing = 1.sp), color = GramaTheme.colors.textTertiary)
                Spacer(Modifier.height(20.dp))
                LinearProgressIndicator(progress = { 0.67f }, modifier = Modifier.fillMaxWidth().height(4.dp), color = AccentPrimary, trackColor = Color.White.copy(0.05f))
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("PROGRESS", style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace), color = GramaTheme.colors.textTertiary)
                    Text("450 / 600 UNIT", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold), color = AccentPrimary)
                }
            }
        }
    }
}
