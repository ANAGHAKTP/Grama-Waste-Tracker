package com.grama.wastetracker.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grama.wastetracker.data.model.WasteAnalysis
import com.grama.wastetracker.data.repository.GeminiRepository
import com.grama.wastetracker.data.repository.ReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

fun Bitmap.downscaleForAnalysis(maxDimension: Int = 1024): Bitmap {
    val scale = maxDimension.toFloat() / maxOf(width, height)
    if (scale >= 1f) return this
    return Bitmap.createScaledBitmap(
        this,
        (width * scale).toInt(),
        (height * scale).toInt(),
        true
    )
}

enum class ReportMode { GENERAL, OFFENDER }

data class ReportState(
    val reportMode: ReportMode = ReportMode.GENERAL,
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

    private var pendingCameraUri: Uri? = null

    fun createImageUri(context: Context): Uri {
        val imageFile = File(
            File(context.cacheDir, "images").also { it.mkdirs() },
            "capture_${System.currentTimeMillis()}.jpg"
        )
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
        pendingCameraUri = uri
        return uri
    }

    fun onCameraCapture(success: Boolean, context: Context) {
        if (success) {
            pendingCameraUri?.let { setImageUri(it, context) }
        }
        if (!success) pendingCameraUri = null
    }

    fun setImageUri(uri: Uri, context: Context) {
        _state.update { it.copy(imageUri = uri) }
        analyzeImageFromUri(uri, context)
    }

    private fun analyzeImageFromUri(uri: Uri, context: Context) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _state.update { it.copy(analyzing = true, error = null) }
            try {
                val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                    android.graphics.ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                
                // Copy bitmap to mutable hardware independent bitmap if needed, but Gemini usually accepts any Bitmap.
                val argbBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                val downscaledBitmap = argbBitmap.downscaleForAnalysis()
                
                val analysis = geminiRepo.analyzeWasteImage(downscaledBitmap)
                val text = "Type: ${analysis.type} | Action: ${analysis.action} | Priority: ${analysis.severity.uppercase()}"
                _state.update {
                    it.copy(
                        analyzing = false,
                        aiAnalysis = analysis,
                        aiAnalysisText = text
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(analyzing = false, error = "Failed to analyze image: ${e.message}") }
            }
        }
    }


    fun updateDescription(desc: String) {
        _state.update { it.copy(description = desc) }
    }

    fun setReportMode(mode: ReportMode) {
        _state.update { it.copy(reportMode = mode) }
    }

    fun submitReport(issueType: String) {
        viewModelScope.launch {
            _state.update { it.copy(submitting = true, error = null) }
            try {
                reportRepo.submitReport(
                    state = _state.value,
                    issueType = issueType,
                    reporterUid = reportRepo.getCurrentUserId()
                )
                _state.update { it.copy(submitting = false, submitted = true) }
            } catch (e: Exception) {
                _state.update { it.copy(submitting = false, error = e.message ?: "Submission failed") }
            }
        }
    }

    fun reset() {
        _state.value = ReportState()
    }
}
