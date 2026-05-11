package com.grama.wastetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grama.wastetracker.data.model.WasteClassification
import com.grama.wastetracker.data.repository.GeminiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class EducationState(
    val query: String = "",
    val searching: Boolean = false,
    val aiResult: WasteClassification? = null,
    val expandedCategory: String? = null
)

class EducationViewModel(
    private val geminiRepo: GeminiRepository = GeminiRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(EducationState())
    val state: StateFlow<EducationState> = _state.asStateFlow()

    fun updateQuery(q: String) {
        _state.value = _state.value.copy(query = q)
    }

    fun searchWasteClassification() {
        val q = _state.value.query.trim()
        if (q.isEmpty() || _state.value.searching) return

        viewModelScope.launch {
            _state.value = _state.value.copy(searching = true, aiResult = null)
            
            // Set an 8-second timeout for the classification
            val result = withTimeoutOrNull(8000) {
                geminiRepo.classifyWasteItem(q)
            }
            
            if (result == null) {
                _state.value = _state.value.copy(
                    searching = false, 
                    aiResult = WasteClassification("Error", "Request timed out. Please check your internet.")
                )
            } else {
                _state.value = _state.value.copy(searching = false, aiResult = result)
            }
        }
    }

    fun toggleCategory(category: String) {
        _state.value = _state.value.copy(
            expandedCategory = if (_state.value.expandedCategory == category) null else category
        )
    }
}
