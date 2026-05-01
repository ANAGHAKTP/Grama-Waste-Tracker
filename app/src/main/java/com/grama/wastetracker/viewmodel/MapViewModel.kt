package com.grama.wastetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MapState(
    val vehicleLat: Double = 12.9716,
    val vehicleLng: Double = 77.5946,
    val eta: Int = 12,
    val distance: Double = 0.8
)

class MapViewModel : ViewModel() {

    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state.asStateFlow()

    init {
        simulateMovement()
    }

    /**
     * Simulates vehicle movement every 30 seconds, matching the web app's setInterval.
     */
    private fun simulateMovement() {
        viewModelScope.launch {
            while (true) {
                delay(30_000)
                _state.value = _state.value.let { current ->
                    current.copy(
                        vehicleLat = current.vehicleLat + 0.0001,
                        vehicleLng = current.vehicleLng + 0.0001,
                        eta = maxOf(1, current.eta - 1)
                    )
                }
            }
        }
    }
}
