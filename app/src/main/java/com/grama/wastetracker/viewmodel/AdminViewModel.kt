package com.grama.wastetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grama.wastetracker.data.model.BlackspotReport
import com.grama.wastetracker.data.repository.GeminiRepository
import com.grama.wastetracker.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminState(
    val reports: List<BlackspotReport> = emptyList(),
    val loading: Boolean = true,
    val summarizing: Boolean = false,
    val aiSummary: String = "",
    val error: String? = null
)

class AdminViewModel(
    private val reportRepo: ReportRepository = ReportRepository(),
    private val geminiRepo: GeminiRepository = GeminiRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(AdminState())
    val state: StateFlow<AdminState> = _state.asStateFlow()

    init {
        observeReports()
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
                    error = e.message
                )
            }
        }
    }

    fun generateSummary() {
        val reports = _state.value.reports
        if (reports.isEmpty() || _state.value.summarizing) return

        viewModelScope.launch {
            _state.value = _state.value.copy(summarizing = true)
            val descriptions = reports.map { "${it.description} (${it.status})" }
            val summary = geminiRepo.generateExecutiveSummary(descriptions)
            _state.value = _state.value.copy(
                summarizing = false,
                aiSummary = summary
            )
        }
    }

    fun resolveReport(reportId: String) {
        viewModelScope.launch {
            try {
                reportRepo.resolveReport(reportId)
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }
}
