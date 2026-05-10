package com.grama.wastetracker.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MapState(
    val vehicleLat: Double = 0.0,
    val vehicleLng: Double = 0.0,
    val userLat: Double? = null,
    val userLng: Double? = null,
    val vehicleRotation: Float = 0f,
    val eta: Int = 0,
    val distanceKm: Double = 0.0,
    val isLoading: Boolean = true, // Force loading until real GPS lock
    val routePoints: List<LatLng> = emptyList()
)

class MapViewModel : ViewModel() {

    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state.asStateFlow()

    private var simulationJob: Job? = null

    /**
     * Syncs the simulation with the user's actual location.
     * This moves the mock truck to your real area.
     */
    fun syncSimulationWithUser(userLocation: LatLng) {
        // Only sync if we haven't found the user yet or they moved significantly
        val currentLat = _state.value.userLat
        val currentLng = _state.value.userLng
        
        if (currentLat != null && currentLng != null) {
            val dist = FloatArray(1)
            Location.distanceBetween(currentLat, currentLng, userLocation.latitude, userLocation.longitude, dist)
            if (dist[0] < 50) return // Stay on current track if movement is minor
        }

        simulationJob?.cancel()
        
        // Place the mock truck in your neighborhood (~500m away)
        val startLat = userLocation.latitude - 0.003
        val startLng = userLocation.longitude - 0.002
        
        _state.value = _state.value.copy(
            userLat = userLocation.latitude,
            userLng = userLocation.longitude,
            vehicleLat = startLat,
            vehicleLng = startLng,
            isLoading = false,
            routePoints = listOf(
                LatLng(startLat, startLng),
                LatLng(userLocation.latitude, userLocation.longitude)
            )
        )
        
        updateLogistics(startLat, startLng, userLocation.latitude, userLocation.longitude)
        startMoving()
    }

    private fun startMoving() {
        simulationJob = viewModelScope.launch {
            while (true) {
                delay(4_000)
                _state.value = _state.value.let { current ->
                    val uLat = current.userLat ?: return@let current
                    val uLng = current.userLng ?: return@let current
                    
                    // Truck moves slowly toward your real location
                    val nextLat = current.vehicleLat + 0.0001
                    val nextLng = current.vehicleLng + 0.00008
                    
                    updateLogistics(nextLat, nextLng, uLat, uLng)
                    
                    current.copy(
                        vehicleLat = nextLat,
                        vehicleLng = nextLng,
                        vehicleRotation = 40f
                    )
                }
            }
        }
    }

    private fun updateLogistics(vLat: Double, vLng: Double, uLat: Double, uLng: Double) {
        val results = FloatArray(1)
        Location.distanceBetween(vLat, vLng, uLat, uLng, results)
        val distanceKm = results[0] / 1000.0
        val eta = (distanceKm / 15.0 * 60).toInt().coerceAtLeast(1)
        
        _state.value = _state.value.copy(
            distanceKm = distanceKm,
            eta = eta
        )
    }
}
