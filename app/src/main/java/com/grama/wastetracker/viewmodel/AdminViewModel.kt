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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
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
            logisticsRepo.observeActiveVehicle()
                .catch { e ->
                    // Handle permission denied or other errors gracefully when signing out
                }
                .collect { vehicle ->
                    _state.update { it.copy(
                        activeVehicleCount = if (vehicle != null) 1 else 0
                    ) }
                }
        }
    }

    private fun observeReports() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            reportRepo.observeReports()
                .catch { e ->
                    _state.update { it.copy(
                        loading = false, 
                        error = "Database link interrupted"
                    ) }
                }
                .collect { reports ->
                    _state.update { it.copy(
                        reports = reports,
                        loading = false
                    ) }
                }
        }
    }

    fun generateSummary() {
        val currentReports = _state.value.reports
        if (currentReports.isEmpty() || _state.value.summarizing) return

        viewModelScope.launch {
            _state.update { it.copy(summarizing = true, error = null) }
            val descriptions = currentReports.map { "${it.description} (${it.status})" }
            
            val summary = withTimeoutOrNull(25000L) {
                geminiRepo.generateExecutiveSummary(descriptions)
            }
            
            _state.update { it.copy(
                summarizing = false,
                aiSummary = summary ?: "AI Engine is currently unresponsive. Check connection."
            ) }
        }
    }

    fun resolveReport(reportId: String) {
        viewModelScope.launch {
            try {
                reportRepo.resolveReport(reportId)
            } catch (e: Exception) {
                _state.update { it.copy(error = "Resolution failed: ${e.message}") }
            }
        }
    }

    fun seedData() {
        refresh()
    }
}
