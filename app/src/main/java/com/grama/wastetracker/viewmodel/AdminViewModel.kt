package com.grama.wastetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grama.wastetracker.data.model.IncidentReport
import com.grama.wastetracker.data.repository.GeminiRepository
import com.grama.wastetracker.data.repository.LogisticsRepository
import com.grama.wastetracker.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class AdminState(
    val reports: List<IncidentReport> = emptyList(),
    val activeVehicleCount: Int = 0,
    val loading: Boolean = true,
    val summarizing: Boolean = false,
    val aiSummary: String = "",
    val error: String? = null
)

class AdminViewModel(
    private val reportRepo: ReportRepository = ReportRepository(),
    private val logisticsRepo: LogisticsRepository = LogisticsRepository(),
    private val geminiRepo: GeminiRepository = GeminiRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(AdminState())
    val state: StateFlow<AdminState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        observeReports()
        observeActiveVehicles()
    }

    private fun observeActiveVehicles() {
        viewModelScope.launch {
            try {
                logisticsRepo.observeActiveVehicle().collect { vehicle ->
                    _state.value = _state.value.copy(
                        activeVehicleCount = if (vehicle != null) 1 else 0
                    )
                }
            } catch (e: Exception) {
                // Background tracking silent fail
            }
        }
    }

    private fun observeReports() {
        viewModelScope.launch {
            try {
                reportRepo.observeReports().collect { reports ->
                    _state.value = _state.value.copy(
                        reports = reports,
                        loading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false, 
                    error = "Database link interrupted"
                )
            }
        }
    }

    fun generateSummary() {
        val currentReports = _state.value.reports
        if (currentReports.isEmpty() || _state.value.summarizing) return

        viewModelScope.launch {
            _state.value = _state.value.copy(summarizing = true, error = null)
            val descriptions = currentReports.map { "${it.description} (${it.status})" }
            
            // Increased timeout and simplified for better inference
            val summary = withTimeoutOrNull(25000L) {
                geminiRepo.generateExecutiveSummary(descriptions)
            }
            
            _state.value = _state.value.copy(
                summarizing = false,
                aiSummary = summary ?: "AI Engine is currently unresponsive. Check connection."
            )
        }
    }

    fun resolveReport(reportId: String) {
        viewModelScope.launch {
            try {
                reportRepo.resolveReport(reportId)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = "Resolution failed: ${e.message}")
            }
        }
    }

    /**
     * Placeholder for seeding demo data from the UI.
     */
    fun seedData() {
        // This is called from the UI hidden debug button
        refresh()
    }
}
