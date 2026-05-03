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

data class DashboardState(
    val dailyInsight: String = "",
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
            try {
                _state.update { it.copy(insightLoading = true, error = null) }
                val insight = geminiRepo.getDailyInsight()
                _state.update { it.copy(dailyInsight = insight, insightLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(insightLoading = false, error = "AI Insight unavailable") }
            }
        }
    }

    private fun fetchSchedules() {
        viewModelScope.launch {
            try {
                val schedules = logisticsRepo.getSchedules()
                _state.update { it.copy(schedules = schedules, error = null) }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to sync schedules") }
            }
        }
    }

    private fun observeVehicle() {
        viewModelScope.launch {
            try {
                logisticsRepo.observeActiveVehicle().collect { vehicle ->
                    _state.update { it.copy(activeVehicle = vehicle, error = null) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Fleet tracking offline") }
            }
        }
    }
    
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
    
    fun refresh() {
        fetchDailyInsight()
        fetchSchedules()
    }
}
