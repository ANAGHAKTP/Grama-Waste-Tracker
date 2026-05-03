package com.grama.wastetracker.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.grama.wastetracker.ui.components.GeometricCard
import com.grama.wastetracker.ui.components.SectionHeader
import com.grama.wastetracker.ui.theme.AccentError
import com.grama.wastetracker.ui.theme.AccentPrimary
import com.grama.wastetracker.ui.theme.GramaTheme
import com.grama.wastetracker.ui.theme.SpaceGroteskFamily
import com.grama.wastetracker.viewmodel.ReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportIssueScreen(
    viewModel: ReportViewModel = viewModel(),
    onSubmitSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    
    // Photo Picker
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.setImageUri(it) }
    }

    var expanded by remember { mutableStateOf(false) }
    val issueTypes = listOf("Illegal Dumping", "Missed Collection", "Overflowing Bin", "Hazardous Material", "Other")
    var selectedIssueType by remember { mutableStateOf(issueTypes[0]) }

    LaunchedEffect(state.submitted) {
        if (state.submitted) {
            onSubmitSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GramaTheme.colors.bgPrimary)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 48.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.ErrorOutline, null, tint = AccentError, modifier = Modifier.size(12.dp))
                Text("INCIDENT LOG", style = MaterialTheme.typography.labelLarge.copy(fontSize = 9.sp, letterSpacing = 2.sp), color = AccentError)
            }
            Spacer(Modifier.height(8.dp))
            Text("Log.Issue", style = MaterialTheme.typography.displaySmall.copy(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold), color = GramaTheme.colors.textPrimary)
        }
        
        HorizontalDivider(color = GramaTheme.colors.borderDim, thickness = 1.dp)

        // Error Banner
        if (state.error != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AccentError.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, AccentError.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Warning, null, tint = AccentError, modifier = Modifier.size(16.dp))
                    Text(state.error ?: "", style = MaterialTheme.typography.bodySmall, color = AccentError)
                }
            }
        }

        // Form
        GeometricCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                
                // Issue Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedIssueType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("ISSUE CATEGORY", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentPrimary,
                            unfocusedBorderColor = GramaTheme.colors.borderDim,
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(GramaTheme.colors.bgSecondary)
                    ) {
                        issueTypes.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption, color = GramaTheme.colors.textPrimary) },
                                onClick = {
                                    selectedIssueType = selectionOption
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Description Field
                OutlinedTextField(
                    value = state.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = { Text("DESCRIPTION / DETAILS", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp)) },
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPrimary,
                        unfocusedBorderColor = GramaTheme.colors.borderDim,
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Photo Upload Area
                Text("EVIDENCE / PHOTO", style = MaterialTheme.typography.labelLarge.copy(fontSize = 9.sp, letterSpacing = 2.sp), color = GramaTheme.colors.textTertiary)
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clickable { launcher.launch("image/*") },
                    shape = RoundedCornerShape(8.dp),
                    color = GramaTheme.colors.bgSecondary,
                    border = BorderStroke(1.dp, GramaTheme.colors.borderDim)
                ) {
                    if (state.imageUri != null) {
                        AsyncImage(
                            model = state.imageUri,
                            contentDescription = "Selected image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                        )
                    } else {
                        Column(
                            Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.AddAPhoto, null, tint = GramaTheme.colors.textTertiary, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("TAP TO UPLOAD PHOTO", style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp), color = GramaTheme.colors.textTertiary)
                        }
                    }
                }

                // AI Analysis Result
                if (state.aiAnalysisText != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = AccentPrimary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.AutoAwesome, null, tint = AccentPrimary, modifier = Modifier.size(16.dp))
                            Text(state.aiAnalysisText ?: "", style = MaterialTheme.typography.bodySmall, color = GramaTheme.colors.textPrimary)
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = { 
                        // Prefix description with issue type if needed
                        if (!state.description.startsWith("[$selectedIssueType]")) {
                            viewModel.updateDescription("[$selectedIssueType] ${state.description}")
                        }
                        viewModel.submitReport() 
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.submitting && state.imageUri != null
                ) {
                    if (state.submitting) {
                        CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Upload, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("SUBMIT REPORT", style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp))
                    }
                }
            }
        }
    }
}
