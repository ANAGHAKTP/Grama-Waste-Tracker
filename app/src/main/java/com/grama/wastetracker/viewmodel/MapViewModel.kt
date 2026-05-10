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
    val isLoading: Boolean = true, // Shows "Establishing Uplink" until GPS fix
    val routePoints: List<LatLng> = emptyList()
)

class MapViewModel : ViewModel() {

    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state.asStateFlow()

    private var simulationJob: Job? = null

    /**
     * Resets the mock truck to a new starting point relative to the user's real location.
     */
    fun syncSimulationWithUser(userLocation: LatLng) {
        if (_state.value.userLat != null) return // Already synced

        simulationJob?.cancel()
        
        // Spawn the truck ~600m away from the user
        val startLat = userLocation.latitude - 0.004
        val startLng = userLocation.longitude - 0.003
        
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
                    if (current.userLat == null) return@let current
                    
                    // Truck moves slowly toward the user's house
                    val nextLat = current.vehicleLat + 0.00012
                    val nextLng = current.vehicleLng + 0.00009
                    
                    updateLogistics(nextLat, nextLng, current.userLat, current.userLng!!)
                    
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
        val eta = (distanceKm / 18.0 * 60).toInt().coerceAtLeast(1)
        
        _state.value = _state.value.copy(
            distanceKm = distanceKm,
            eta = eta
        )
    }
}
