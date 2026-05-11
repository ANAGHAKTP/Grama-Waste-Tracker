package com.grama.wastetracker.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.grama.wastetracker.BuildConfig
import com.grama.wastetracker.data.model.WasteAnalysis
import com.grama.wastetracker.data.model.WasteClassification
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject

/**
 * Repository for Google Gemini AI interactions.
 * Handles AI calls for Dashboard tips, Image Analysis, Waste Classification, and Admin Summaries.
 */
class GeminiRepository {

    private val textModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private val visionModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    /**
     * Generate a daily waste management tip for the Dashboard.
     */
    suspend fun getDailyInsight(): String {
        return try {
            val response = withTimeoutOrNull(5000L) {
                textModel.generateContent("Generate a 1-sentence helpful tip for a rural village resident about waste management. Be encouraging and simple.")
            }
            response?.text ?: FALLBACK_INSIGHT
        } catch (e: Exception) {
            Log.e("GeminiRepo", "Insight error: ${e.message}")
            FALLBACK_INSIGHT
        }
    }

    /**
     * Analyze a waste image using multimodal (vision) capabilities.
     */
    suspend fun analyzeWasteImage(bitmap: Bitmap): WasteAnalysis {
        return try {
            val result = withTimeoutOrNull(15000L) {
                val prompt = content {
                    image(bitmap)
                    text("Analyze this image of waste. Return JSON: { \"type\": \"...\", \"action\": \"...\", \"severity\": \"low\" | \"medium\" | \"high\" }")
                }
                visionModel.generateContent(prompt).text
            } ?: throw Exception("Timeout")

            val jsonStr = result.replace("```json", "").replace("```", "").trim()
            val json = JSONObject(jsonStr)
            WasteAnalysis(
                type = json.optString("type", "Unknown"),
                action = json.optString("action", "Dispose per local guidelines"),
                severity = json.optString("severity", "medium")
            )
        } catch (e: Exception) {
            Log.e("GeminiRepo", "Vision Error: ${e.message}")
            WasteAnalysis(type = "Service Offline", action = "Please check your connection.", severity = "low")
        }
    }

    /**
     * Classify a waste item by name (text-based).
     */
    suspend fun classifyWasteItem(itemName: String): WasteClassification {
        return try {
            val result = withTimeoutOrNull(8000L) {
                val response = textModel.generateContent(
                    "Classify waste category for '$itemName'. Return JSON: { \"category\": \"...\", \"instruction\": \"...\" }"
                )
                response.text
            } ?: throw Exception("Timeout")

            val jsonStr = result.replace("```json", "").replace("```", "").trim()
            val json = JSONObject(jsonStr)
            WasteClassification(
                category = json.optString("category", "Unknown"),
                instruction = json.optString("instruction", "Consult guidelines.")
            )
        } catch (e: Exception) {
            WasteClassification("Unknown", "AI Service Unavailable")
        }
    }

    /**
     * Generate an executive summary for the Admin dashboard based on report descriptions.
     */
    suspend fun generateExecutiveSummary(reportDescriptions: List<String>): String {
        return try {
            val reportList = reportDescriptions.joinToString("\n") { "- $it" }
            val response = withTimeoutOrNull(12000L) {
                textModel.generateContent("Provide a 2-sentence executive summary for the Village Panchayat based on these reports:\n$reportList")
            }
            response?.text ?: "No summary available."
        } catch (e: Exception) {
            "AI summary generation failed."
        }
    }

    companion object {
        private const val FALLBACK_INSIGHT = "Keep your village clean, start composting today!"
    }
}
