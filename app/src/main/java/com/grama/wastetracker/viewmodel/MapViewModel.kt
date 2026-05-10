package com.grama.wastetracker.viewmodel

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
    val vehicleLat: Double = 12.9716,
    val vehicleLng: Double = 77.5946,
    val vehicleRotation: Float = 0f,
    val eta: Int = 12,
    val distance: Double = 0.8,
    val isLoading: Boolean = false,
    val routePoints: List<LatLng> = emptyList()
)

class MapViewModel : ViewModel() {

    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state.asStateFlow()

    private var simulationJob: Job? = null

    init {
        // Initialize with default mock route
        resetSimulation(LatLng(12.9716, 77.5946))
    }

    /**
     * Resets the mock truck to a new starting point (e.g., the user's location)
     */
    fun resetSimulation(startLocation: LatLng) {
        simulationJob?.cancel()
        
        _state.value = _state.value.copy(
            vehicleLat = startLocation.latitude,
            vehicleLng = startLocation.longitude,
            routePoints = listOf(
                startLocation,
                LatLng(startLocation.latitude + 0.001, startLocation.longitude + 0.001),
                LatLng(startLocation.latitude + 0.002, startLocation.longitude + 0.002)
            )
        )
        
        startMoving()
    }

    private fun startMoving() {
        simulationJob = viewModelScope.launch {
            var step = 0
            while (true) {
                delay(5_000)
                _state.value = _state.value.let { current ->
                    step++
                    current.copy(
                        vehicleLat = current.vehicleLat + 0.0001,
                        vehicleLng = current.vehicleLng + 0.0001,
                        vehicleRotation = 45f,
                        eta = maxOf(1, current.eta - 1),
                        distance = maxOf(0.1, current.distance - 0.05)
                    )
                }
            }
        }
    }
}
