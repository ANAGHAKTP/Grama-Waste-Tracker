package com.grama.wastetracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.grama.wastetracker.ui.components.GeometricCard
import com.grama.wastetracker.ui.theme.AccentError
import com.grama.wastetracker.ui.theme.AccentPrimary
import com.grama.wastetracker.ui.theme.GramaTheme
import com.grama.wastetracker.ui.theme.SpaceGroteskFamily
import com.grama.wastetracker.viewmodel.ReportMode
import com.grama.wastetracker.viewmodel.ReportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportIssueScreen(
    viewModel: ReportViewModel = viewModel(),
    onSubmitSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    // Camera launchers
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> viewModel.onCameraCapture(success, context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = viewModel.createImageUri(context)
            cameraLauncher.launch(uri)
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.setImageUri(it, context) } }

    var selectedIssueType by remember { mutableStateOf("Illegal Dumping") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    val issueTypes = listOf(
        "Illegal Dumping", "Missed Collection",
        "Overflowing Bin", "Hazardous Material", "Other"
    )

    LaunchedEffect(state.submitted) {
        if (state.submitted) onSubmitSuccess()
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.ErrorOutline, null,
                    tint = AccentError,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    "INCIDENT LOG",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 9.sp, letterSpacing = 2.sp
                    ),
                    color = AccentError
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Log.Issue",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold
                ),
                color = GramaTheme.colors.textPrimary
            )
        }

        HorizontalDivider(color = GramaTheme.colors.borderDim, thickness = 1.dp)

        // Segmented toggle - Refined for higher contrast in Dark Mode
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(GramaTheme.colors.bgSecondary)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ReportMode.entries.forEach { mode ->
                val selected = state.reportMode == mode
                val label = if (mode == ReportMode.GENERAL) "General Issue" else "Report Offender"
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.setReportMode(mode) },
                    color = if (selected) {
                        if (isDark) Color.White.copy(alpha = 0.9f) else AccentPrimary
                    } else Color.Transparent,
                    shape = RoundedCornerShape(8.dp),
                    border = if (selected && isDark) null else BorderStroke(0.dp, Color.Transparent)
                ) {
                    Text(
                        label,
                        modifier = Modifier.padding(vertical = 12.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 0.5.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (selected) {
                            if (isDark) Color.Black else Color.White
                        } else GramaTheme.colors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        // Error banner
        if (state.error != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AccentError.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, AccentError.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Warning, null, tint = AccentError,
                         modifier = Modifier.size(16.dp))
                    Text(state.error ?: "",
                         style = MaterialTheme.typography.bodySmall,
                         color = AccentError)
                }
            }
        }

        // Form card - Updated with premium elevation and theme-aware borders
        GeometricCard(
            modifier = Modifier.fillMaxWidth(),
            elevation = if (isDark) 0.dp else 4.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {

                // Issue type — only shown in GENERAL mode
                if (state.reportMode == ReportMode.GENERAL) {
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedIssueType,
                            onValueChange = {},
                            readOnly = true,
                            label = {
                                Text("ISSUE CATEGORY",
                                     style = MaterialTheme.typography.labelSmall
                                         .copy(letterSpacing = 1.sp))
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(dropdownExpanded)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentPrimary,
                                unfocusedBorderColor = GramaTheme.colors.borderDim,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.background(GramaTheme.colors.bgSecondary)
                        ) {
                            issueTypes.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(option, color = GramaTheme.colors.textPrimary)
                                    },
                                    onClick = {
                                        selectedIssueType = option
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Offender mode notice
                if (state.reportMode == ReportMode.OFFENDER) {
                    Surface(
                        color = AccentError.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, AccentError.copy(alpha = 0.2f))
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, null,
                                 tint = AccentError, modifier = Modifier.size(16.dp))
                            Text(
                                "Reports are reviewed by the Panchayat ward officer. " +
                                "Photo evidence is required.",
                                style = MaterialTheme.typography.bodySmall,
                                color = GramaTheme.colors.textSecondary
                            )
                        }
                    }
                }

                // Description
                OutlinedTextField(
                    value = state.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = {
                        Text(
                            if (state.reportMode == ReportMode.OFFENDER)
                                "DESCRIBE THE INCIDENT"
                            else "DESCRIPTION / DETAILS",
                            style = MaterialTheme.typography.labelSmall
                                .copy(letterSpacing = 1.sp)
                        )
                    },
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPrimary,
                        unfocusedBorderColor = GramaTheme.colors.borderDim,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Photo capture
                Text(
                    "EVIDENCE / PHOTO",
                    style = MaterialTheme.typography.labelLarge
                        .copy(fontSize = 9.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold),
                    color = GramaTheme.colors.textTertiary
                )

                if (state.imageUri != null && state.imageUri != Uri.EMPTY) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(GramaTheme.colors.bgPrimary)) {
                        AsyncImage(
                            model = state.imageUri,
                            contentDescription = "Evidence photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                        IconButton(
                            onClick = { viewModel.setImageUri(Uri.EMPTY, context) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.5f),
                                    CircleShape
                                )
                                .size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, null,
                                 tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = {
                                permissionLauncher.launch(
                                    android.Manifest.permission.CAMERA
                                )
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else GramaTheme.colors.borderDim),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = GramaTheme.colors.bgPrimary.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(Icons.Default.CameraAlt, null,
                                 modifier = Modifier.size(20.dp),
                                 tint = AccentPrimary)
                            Spacer(Modifier.width(8.dp))
                            Text("Camera",
                                 style = MaterialTheme.typography.labelMedium,
                                 color = GramaTheme.colors.textPrimary)
                        }
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else GramaTheme.colors.borderDim),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = GramaTheme.colors.bgPrimary.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(Icons.Default.Photo, null,
                                 modifier = Modifier.size(20.dp),
                                 tint = GramaTheme.colors.textSecondary)
                            Spacer(Modifier.width(8.dp))
                            Text("Gallery",
                                 style = MaterialTheme.typography.labelMedium,
                                 color = GramaTheme.colors.textPrimary)
                        }
                    }
                }

                // AI analysis result
                if (state.aiAnalysisText != null) {
                    Surface(
                        color = AccentPrimary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, AccentPrimary.copy(alpha = 0.2f))
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, null,
                                 tint = AccentPrimary, modifier = Modifier.size(16.dp))
                            Text(state.aiAnalysisText ?: "",
                                 style = MaterialTheme.typography.bodySmall,
                                 color = GramaTheme.colors.textPrimary)
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                // Submit
                Button(
                    onClick = { viewModel.submitReport(selectedIssueType) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentPrimary,
                        disabledContainerColor = GramaTheme.colors.bgTertiary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.submitting && state.imageUri != null
                              && state.imageUri != Uri.EMPTY
                ) {
                    if (state.submitting) {
                        CircularProgressIndicator(
                            Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Upload, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (state.reportMode == ReportMode.OFFENDER)
                                "SUBMIT OFFENDER REPORT"
                            else "SUBMIT REPORT",
                            style = MaterialTheme.typography.labelLarge
                                .copy(letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
