package com.grama.wastetracker.viewmodel

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.grama.wastetracker.data.repository.LogisticsRepository
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

class MapViewModel(
    private val logisticsRepository: LogisticsRepository = LogisticsRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state.asStateFlow()

    init {
        observeVehicle()
    }

    private fun observeVehicle() {
        viewModelScope.launch {
            logisticsRepository.observeActiveVehicle().collect { vehicle ->
                vehicle?.let {
                    val newPos = LatLng(it.location.lat, it.location.lng)
                    val current = _state.value
                    
                    // Calculate rotation if we have a previous position
                    val rotation = if (current.vehicleLat != 0.0) {
                        calculateBearing(
                            current.vehicleLat, current.vehicleLng,
                            newPos.latitude, newPos.longitude
                        )
                    } else 0f
                    
                    updateVehicleLocation(newPos, rotation) 
                }
            }
        }
    }

    private fun calculateBearing(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val loc1 = Location("").apply { latitude = lat1; longitude = lng1 }
        val loc2 = Location("").apply { latitude = lat2; longitude = lng2 }
        return loc1.bearingTo(loc2)
    }

    fun updateUserLocation(userLocation: LatLng) {
        _state.update { it.copy(
            userLat = userLocation.latitude,
            userLng = userLocation.longitude,
            isLoading = false
        )}
    }

    fun updateVehicleLocation(newVehiclePos: LatLng, rotation: Float) {
        _state.update { current ->
            val uLat = current.userLat ?: return@update current.copy(
                vehicleLat = newVehiclePos.latitude,
                vehicleLng = newVehiclePos.longitude,
                vehicleRotation = rotation
            )
            val uLng = current.userLng ?: return@update current.copy(
                vehicleLat = newVehiclePos.latitude,
                vehicleLng = newVehiclePos.longitude,
                vehicleRotation = rotation
            )
            
            val distResults = FloatArray(1)
            Location.distanceBetween(newVehiclePos.latitude, newVehiclePos.longitude, uLat, uLng, distResults)
            val distanceMeters = distResults[0]
            
            val distanceKm = distanceMeters / 1000.0
            val eta = (distanceKm / 15.0 * 60).toInt().coerceAtLeast(1)

            current.copy(
                vehicleLat = newVehiclePos.latitude,
                vehicleLng = newVehiclePos.longitude,
                vehicleRotation = rotation,
                distanceKm = distanceKm,
                eta = eta,
                isArrived = distanceMeters < 30,
                routePoints = listOf(newVehiclePos, LatLng(uLat, uLng))
            )
        }
    }

    companion object {
        fun factory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MapViewModel(LogisticsRepository()) as T
            }
        }
    }
}
