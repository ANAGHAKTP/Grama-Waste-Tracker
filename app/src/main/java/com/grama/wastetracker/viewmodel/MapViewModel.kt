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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapState(
    val vehicleLat: Double = 0.0,
    val vehicleLng: Double = 0.0,
    val userLat: Double? = null,
    val userLng: Double? = null,
    val vehicleRotation: Float = 0f,
    val eta: Int = 0,
    val distanceKm: Double = 0.0,
    val isLoading: Boolean = true,
    val isArrived: Boolean = false,
    val routePoints: List<LatLng> = emptyList()
)

class MapViewModel : ViewModel() {

    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state.asStateFlow()

    private var simulationJob: Job? = null

    /**
     * Resets simulation and starts moving toward the user's real area.
     */
    fun syncSimulationWithUser(userLocation: LatLng) {
        if (_state.value.userLat != null) return 

        simulationJob?.cancel()
        
        // Spawn truck ~600m away from user
        val startLat = userLocation.latitude - 0.004
        val startLng = userLocation.longitude - 0.003
        
        _state.update { it.copy(
            userLat = userLocation.latitude,
            userLng = userLocation.longitude,
            vehicleLat = startLat,
            vehicleLng = startLng,
            isLoading = false,
            routePoints = listOf(LatLng(startLat, startLng), userLocation)
        )}
        
        startMoving()
    }

    private fun startMoving() {
        simulationJob = viewModelScope.launch {
            while (true) {
                delay(5000) // Heartbeat every 5 seconds for stability
                
                var arrived = false
                _state.update { current ->
                    val uLat = current.userLat ?: return@update current
                    val uLng = current.userLng ?: return@update current
                    
                    val distResults = FloatArray(1)
                    Location.distanceBetween(current.vehicleLat, current.vehicleLng, uLat, uLng, distResults)
                    val distanceMeters = distResults[0]

                    if (distanceMeters < 15) {
                        arrived = true
                        return@update current.copy(isArrived = true, distanceKm = 0.0, eta = 0)
                    }

                    // Move truck closer to user
                    val nextLat = current.vehicleLat + (uLat - current.vehicleLat) * 0.1
                    val nextLng = current.vehicleLng + (uLng - current.vehicleLng) * 0.1
                    
                    // Calculate bearing (rotation)
                    val startLoc = Location("").apply { latitude = current.vehicleLat; longitude = current.vehicleLng }
                    val endLoc = Location("").apply { latitude = nextLat; longitude = nextLng }
                    val bearing = startLoc.bearingTo(endLoc)
                    
                    val distanceKm = distanceMeters / 1000.0
                    val eta = (distanceKm / 15.0 * 60).toInt().coerceAtLeast(1)

                    current.copy(
                        vehicleLat = nextLat,
                        vehicleLng = nextLng,
                        vehicleRotation = bearing,
                        distanceKm = distanceKm,
                        eta = eta
                    )
                }
                if (arrived) break
            }
        }
    }
}
