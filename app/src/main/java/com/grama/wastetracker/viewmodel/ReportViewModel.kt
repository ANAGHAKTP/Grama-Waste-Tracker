package com.grama.wastetracker.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grama.wastetracker.data.model.WasteAnalysis
import com.grama.wastetracker.data.repository.GeminiRepository
import com.grama.wastetracker.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportState(
    val imageUri: Uri? = null,
    val analyzing: Boolean = false,
    val aiAnalysis: WasteAnalysis? = null,
    val aiAnalysisText: String? = null,
    val description: String = "",
    val submitting: Boolean = false,
    val submitted: Boolean = false,
    val error: String? = null
)

class ReportViewModel(
    private val reportRepo: ReportRepository = ReportRepository(),
    private val geminiRepo: GeminiRepository = GeminiRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(ReportState())
    val state: StateFlow<ReportState> = _state.asStateFlow()

    fun setImageUri(uri: Uri) {
        _state.value = _state.value.copy(imageUri = uri)
    }

    fun analyzeImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _state.value = _state.value.copy(analyzing = true)
            val analysis = geminiRepo.analyzeWasteImage(bitmap)
            val text = "Type: ${analysis.type} | Action: ${analysis.action} | Priority: ${analysis.severity.uppercase()}"
            _state.value = _state.value.copy(
                analyzing = false,
                aiAnalysis = analysis,
                aiAnalysisText = text
            )
        }
    }

    fun updateDescription(desc: String) {
        _state.value = _state.value.copy(description = desc)
    }

    fun submitReport() {
        val uri = _state.value.imageUri ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(submitting = true, error = null)
            try {
                reportRepo.submitReport(
                    imageUri = uri,
                    description = _state.value.description,
                    aiAnalysis = _state.value.aiAnalysisText
                )
                _state.value = _state.value.copy(submitting = false, submitted = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    submitting = false,
                    error = e.message ?: "Submission failed"
                )
            }
        }
    }

    fun reset() {
        _state.value = ReportState()
    }
}
