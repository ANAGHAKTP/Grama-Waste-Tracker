package com.grama.wastetracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.*
import com.grama.wastetracker.ui.components.GeometricCard
import com.grama.wastetracker.ui.theme.*
import com.grama.wastetracker.viewmodel.MapViewModel

@Composable
fun LiveMapScreen(
    mapViewModel: MapViewModel = viewModel()
) {
    val state by mapViewModel.state.collectAsState()

    val vehiclePosition = com.google.android.gms.maps.model.LatLng(state.vehicleLat, state.vehicleLng)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(vehiclePosition, 15f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Google Map ──
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(zoomControlsEnabled = false)
        ) {
            Marker(
                state = MarkerState(position = vehiclePosition),
                title = "Truck KA-01-1234",
                snippet = "Speed: 15 km/h",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)
            )
        }

        // ── Floating Status Card (Top) ──
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp, start = 16.dp, end = 16.dp)
        ) {
            GeometricCard(
                elevation = 16.dp,
                contentPadding = 16.dp
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
                                modifier = Modifier.size(24.dp)
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
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = GramaTheme.colors.textTertiary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "${state.eta} MIN",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = GramaTheme.colors.textTertiary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(
                                        GramaTheme.colors.borderDim,
                                        RoundedCornerShape(50)
                                    )
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Route,
                                    contentDescription = null,
                                    tint = GramaTheme.colors.textTertiary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "0.8 KM",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = GramaTheme.colors.textTertiary
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Bottom Info Card ──
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            color = GramaTheme.colors.bgTertiary,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 16.dp,
            border = BorderStroke(1.dp, GramaTheme.colors.borderDim)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        border = BorderStroke(1.dp, GramaTheme.colors.borderDim),
                        color = Color.Transparent,
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
                            style = MaterialTheme.typography.labelMedium.copy(
                                letterSpacing = 0.sp
                            ),
                            color = GramaTheme.colors.textTertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "ACKNOWLEDGE",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 10.sp,
                            letterSpacing = 2.sp,
                        ),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
