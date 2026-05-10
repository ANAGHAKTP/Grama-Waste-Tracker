package com.grama.wastetracker.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import com.grama.wastetracker.R
import com.grama.wastetracker.ui.components.GeometricCard
import com.grama.wastetracker.ui.theme.*
import com.grama.wastetracker.viewmodel.MapViewModel

@Composable
fun LiveMapScreen(
    mapViewModel: MapViewModel = viewModel()
) {
    val state by mapViewModel.state.collectAsState()
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    
    var isProtocolAcknowledged by remember { mutableStateOf(false) }
    var followVehicle by remember { mutableStateOf(true) }
    
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasLocationPermission = isGranted }
    )

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val vehiclePosition = com.google.android.gms.maps.model.LatLng(state.vehicleLat, state.vehicleLng)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(vehiclePosition, 16f)
    }

    // Smoothly track vehicle if "Follow" is enabled
    LaunchedEffect(vehiclePosition, followVehicle) {
        if (followVehicle) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLng(vehiclePosition),
                durationMs = 1000
            )
        }
    }

    val mapStyle = remember(isDark) {
        MapStyleOptions.loadRawResourceStyle(
            context,
            if (isDark) R.raw.map_style_dark else R.raw.map_style_light
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(modifier = Modifier.fillMaxSize().background(GramaTheme.colors.bgPrimary)) {
        // ── Google Map ──
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                mapStyleOptions = mapStyle,
                isMyLocationEnabled = hasLocationPermission
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                compassEnabled = true
            ),
            onMapClick = { followVehicle = false } // Disable follow on manual interaction
        ) {
            // Draw Route Path (Polyline)
            if (state.routePoints.isNotEmpty()) {
                Polyline(
                    points = state.routePoints,
                    color = AccentPrimary.copy(alpha = 0.6f),
                    width = 12f,
                    jointType = com.google.android.gms.maps.model.JointType.ROUND
                )
            }

            // Vehicle Marker
            MarkerComposable(
                state = MarkerState(position = vehiclePosition),
                title = "Unit GA-01-1234",
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        Modifier
                            .size(32.dp * pulseScale)
                            .clip(CircleShape)
                            .background(AccentPrimary.copy(alpha = pulseAlpha))
                    )
                    Surface(
                        shape = CircleShape,
                        color = AccentPrimary,
                        border = BorderStroke(2.dp, Color.White),
                        shadowElevation = 8.dp,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .padding(4.dp)
                                .rotate(state.vehicleRotation) // Applied vehicle heading
                        )
                    }
                }
            }
        }

        if (state.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                color = AccentPrimary
            )
        }

        // ── Floating Status Card (Top) ──
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp, start = 20.dp, end = 20.dp)
        ) {
            GeometricCard(
                elevation = 12.dp,
                backgroundColor = if (isDark) GramaTheme.colors.bgSecondary else Color.White,
                borderColor = GramaTheme.colors.borderDim.copy(alpha = 0.5f),
                contentPadding = 20.dp
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        border = BorderStroke(1.dp, GramaTheme.colors.borderDim),
                        color = Color.Transparent,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                tint = AccentPrimary,
                                modifier = Modifier.size(24.dp).rotate(state.vehicleRotation)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "LOGISTICS STATUS",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 9.sp,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = GramaTheme.colors.textPrimary
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusItem(Icons.Default.Schedule, "${state.eta} MIN")
                            DividerDot()
                            StatusItem(Icons.Default.Route, "${"%.1f".format(state.distance)} KM")
                        }
                    }
                }
            }
        }

        // ── Map Controls ──
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Re-center/Follow Toggle
            FloatingActionButton(
                onClick = { 
                    followVehicle = true
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(vehiclePosition, 16f)
                },
                containerColor = if (followVehicle) AccentPrimary else GramaTheme.colors.bgSecondary,
                contentColor = if (followVehicle) Color.White else AccentPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = if (followVehicle) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed,
                    contentDescription = "Toggle Follow"
                )
            }
        }

        // ── Bottom Info Card (Dismissible) ──
        AnimatedVisibility(
            visible = !isProtocolAcknowledged,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 110.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth()
        ) {
            GeometricCard(
                elevation = 16.dp,
                backgroundColor = if (isDark) GramaTheme.colors.bgTertiary else Color(0xFFF3F4FF),
                borderColor = AccentPrimary.copy(alpha = 0.1f),
                contentPadding = 24.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, GramaTheme.colors.borderDim),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = AccentPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "SYSTEM PROTOCOL",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 10.sp,
                                    letterSpacing = 2.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GramaTheme.colors.textPrimary
                            )
                            Text(
                                text = "Please execute dry/wet segregation prior to collection arrival.",
                                style = MaterialTheme.typography.labelMedium,
                                color = GramaTheme.colors.textSecondary
                            )
                        }
                    }

                    Button(
                        onClick = { isProtocolAcknowledged = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPrimary),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "ACKNOWLEDGE",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 10.sp,
                                letterSpacing = 2.sp,
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = GramaTheme.colors.textTertiary,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace
            ),
            color = GramaTheme.colors.textTertiary
        )
    }
}

@Composable
private fun DividerDot() {
    Box(
        modifier = Modifier
            .size(4.dp)
            .background(
                GramaTheme.colors.borderDim,
                RoundedCornerShape(50)
            )
    )
}
