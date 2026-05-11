package com.grama.wastetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grama.wastetracker.data.model.Schedule
import com.grama.wastetracker.data.model.Vehicle
import com.grama.wastetracker.data.repository.GeminiRepository
import com.grama.wastetracker.data.repository.LogisticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class DashboardState(
    val dailyInsight: String = "Keep your village clean, start composting today!",
    val insightLoading: Boolean = true,
    val activeVehicle: Vehicle? = null,
    val schedules: List<Schedule> = emptyList(),
    val error: String? = null
)

class DashboardViewModel(
    private val geminiRepo: GeminiRepository = GeminiRepository(),
    private val logisticsRepo: LogisticsRepository = LogisticsRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        fetchDailyInsight()
        fetchSchedules()
        observeVehicle()
    }

    private fun fetchDailyInsight() {
        viewModelScope.launch {
            _state.update { it.copy(insightLoading = true) }
            // Set a strict 5-second timeout for the non-essential AI tip
            val insight = withTimeoutOrNull(5000) {
                geminiRepo.getDailyInsight()
            }
            _state.update { it.copy(
                dailyInsight = insight ?: "Keep your village clean, start composting today!",
                insightLoading = false 
            ) }
        }
    }

    private fun fetchSchedules() {
        viewModelScope.launch {
            try {
                val schedules = logisticsRepo.getSchedules()
                _state.update { it.copy(schedules = schedules) }
            } catch (e: Exception) {
                // Gracefully ignore schedule fetch errors for demo
            }
        }
    }

    private fun observeVehicle() {
        viewModelScope.launch {
            logisticsRepo.observeActiveVehicle().collect { vehicle ->
                _state.update { it.copy(activeVehicle = vehicle) }
            }
        }
    }
    
    fun refresh() {
        fetchDailyInsight()
        fetchSchedules()
    }
}
