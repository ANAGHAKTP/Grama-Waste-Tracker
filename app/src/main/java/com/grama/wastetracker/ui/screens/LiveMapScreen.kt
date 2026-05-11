package com.grama.wastetracker.ui.screens

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import com.grama.wastetracker.R
import com.grama.wastetracker.ui.components.GeometricCard
import com.grama.wastetracker.ui.theme.*
import com.grama.wastetracker.viewmodel.MapViewModel

@SuppressLint("MissingPermission")
@Composable
fun LiveMapScreen(
    mapViewModel: MapViewModel = viewModel()
) {
    val state by mapViewModel.state.collectAsState()
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
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

    // Force fetch fresh user coordinates
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            val cts = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { location ->
                    location?.let {
                        mapViewModel.syncSimulationWithUser(LatLng(it.latitude, it.longitude))
                    }
                }
        } else {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val vehiclePosition = LatLng(state.vehicleLat, state.vehicleLng)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(vehiclePosition, 16f)
    }

    // Stable MarkerState to prevent jitter
    val truckMarkerState = rememberMarkerState(position = vehiclePosition)
    LaunchedEffect(vehiclePosition) {
        truckMarkerState.position = vehiclePosition
    }

    // Smooth Camera Tracking
    LaunchedEffect(vehiclePosition, followVehicle) {
        if (followVehicle && !state.isLoading && !state.isArrived) {
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLng(vehiclePosition),
                durationMs = 2000 
            )
        }
    }

    // Animate rotation smoothly
    val animatedRotation by animateFloatAsState(
        targetValue = state.vehicleRotation,
        animationSpec = tween(1000),
        label = "rotation"
    )

    val mapStyle = remember(isDark) {
        MapStyleOptions.loadRawResourceStyle(
            context,
            if (isDark) R.raw.map_style_dark else R.raw.map_style_light
        )
    }

    // Pulse animation for the separate circle overlay (stops arrow from jittering)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val radiusPulse by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 100f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label = "radius"
    )

    Box(modifier = Modifier.fillMaxSize().background(GramaTheme.colors.bgPrimary)) {
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AccentPrimary)
                    Spacer(Modifier.height(16.dp))
                    Text("Acquiring GPS...", style = MaterialTheme.typography.labelMedium, color = GramaTheme.colors.textPrimary)
                }
            }
        }

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
            onMapClick = { followVehicle = false }
        ) {
            if (state.routePoints.isNotEmpty()) {
                Polyline(
                    points = state.routePoints,
                    color = AccentPrimary.copy(alpha = 0.4f),
                    width = 8f,
                    jointType = com.google.android.gms.maps.model.JointType.ROUND
                )
            }

            // Pulsing Range Circle (Separated from Marker to stop jitter)
            if (!state.isLoading && !state.isArrived) {
                Circle(
                    center = vehiclePosition,
                    radius = radiusPulse.toDouble(),
                    fillColor = AccentPrimary.copy(alpha = 0.1f),
                    strokeColor = AccentPrimary.copy(alpha = 0.2f),
                    strokeWidth = 2f
                )
            }

            // User's Destination Marker
            val uLat = state.userLat
            val uLng = state.userLng
            if (uLat != null && uLng != null) {
                Marker(
                    state = rememberMarkerState(position = LatLng(uLat, uLng)),
                    title = "Your Home",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }

            // Vehicle Marker
            MarkerComposable(
                state = truckMarkerState,
                anchor = Offset(0.5f, 0.5f), // Rotate around center
                title = if (state.isArrived) "Arrived" else "Unit GA-01-1234",
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (state.isArrived) AccentTertiary else AccentPrimary,
                    border = BorderStroke(2.dp, Color.White),
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (state.isArrived) Icons.Default.Check else Icons.Default.Navigation,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .padding(4.dp)
                            .rotate(if (state.isArrived) 0f else animatedRotation)
                    )
                }
            }
        }

        // ── Logistics HUD ──
        AnimatedVisibility(
            visible = !state.isLoading,
            enter = slideInVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 56.dp, start = 20.dp, end = 20.dp)
        ) {
            GeometricCard(
                elevation = 12.dp,
                backgroundColor = if (isDark) GramaTheme.colors.bgSecondary else Color.White,
                contentPadding = 20.dp
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        color = (if (state.isArrived) AccentTertiary else AccentPrimary).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (state.isArrived) Icons.Default.CheckCircle else Icons.Default.Navigation,
                                contentDescription = null,
                                tint = if (state.isArrived) AccentTertiary else AccentPrimary,
                                modifier = Modifier.size(24.dp).rotate(state.vehicleRotation)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (state.isArrived) "ARRIVAL CONFIRMED" else "LOGISTICS STATUS",
                            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Bold),
                            color = if (state.isArrived) AccentTertiary else GramaTheme.colors.textPrimary
                        )
                        if (!state.isArrived) {
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                StatusItem(Icons.Default.Schedule, "${state.eta} MIN")
                                DividerDot()
                                StatusItem(Icons.Default.Route, "${"%.2f".format(state.distanceKm)} KM")
                            }
                        }
                    }
                }
            }
        }

        // FAB Controls
        if (!state.isLoading) {
            FloatingActionButton(
                onClick = { 
                    followVehicle = true
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(vehiclePosition, 16f)
                },
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
                containerColor = if (followVehicle) AccentPrimary else GramaTheme.colors.bgSecondary,
                contentColor = if (followVehicle) Color.White else AccentPrimary,
                shape = CircleShape
            ) {
                Icon(imageVector = if (followVehicle) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed, contentDescription = null)
            }
        }
    }
}

@Composable
private fun StatusItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = GramaTheme.colors.textTertiary, modifier = Modifier.size(12.dp))
        Text(text, style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace), color = GramaTheme.colors.textTertiary)
    }
}

@Composable
private fun DividerDot() {
    Box(Modifier.size(4.dp).background(GramaTheme.colors.borderDim, CircleShape))
}
