package com.grama.wastetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grama.wastetracker.data.repository.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardState(
    val dailyInsight: String = "",
    val insightLoading: Boolean = true
)

class DashboardViewModel(
    private val geminiRepo: GeminiRepository = GeminiRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        fetchDailyInsight()
    }

    private fun fetchDailyInsight() {
        viewModelScope.launch {
            _state.value = _state.value.copy(insightLoading = true)
            val insight = geminiRepo.getDailyInsight()
            _state.value = _state.value.copy(
                dailyInsight = insight,
                insightLoading = false
            )
        }
    }
}
