package com.grama.wastetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grama.wastetracker.data.model.IncidentReport
import com.grama.wastetracker.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyReportsState(
    val reports: List<IncidentReport> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null
)

class MyReportsViewModel(
    private val reportRepo: ReportRepository = ReportRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(MyReportsState())
    val state: StateFlow<MyReportsState> = _state.asStateFlow()

    init {
        observeUserReports()
    }

    private fun observeUserReports() {
        val uid = reportRepo.getCurrentUserId()
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            reportRepo.observeUserReports(uid)
                .catch { e ->
                    _state.update { it.copy(loading = false, error = e.message) }
                }
                .collect { reports ->
                    _state.update { it.copy(reports = reports, loading = false) }
                }
        }
    }
}
