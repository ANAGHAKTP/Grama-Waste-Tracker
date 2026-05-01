package com.grama.wastetracker.data.repository

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.grama.wastetracker.BuildConfig
import com.grama.wastetracker.data.model.WasteAnalysis
import com.grama.wastetracker.data.model.WasteClassification
import org.json.JSONObject

/**
 * Repository for Google Gemini AI interactions.
 * Mirrors all AI calls from Dashboard, ReportIssue, Education, and AdminDashboard.
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
            val response = textModel.generateContent(
                "Generate a 1-sentence helpful tip for a rural village resident about waste management, composting, or recycling. Be encouraging and simple."
            )
            response.text ?: FALLBACK_INSIGHT
        } catch (e: Exception) {
            FALLBACK_INSIGHT
        }
    }

    /**
     * Analyze a waste image using multimodal (vision) capabilities.
     * Returns structured analysis with type, action, and severity.
     */
    suspend fun analyzeWasteImage(bitmap: Bitmap): WasteAnalysis {
        return try {
            val prompt = content {
                image(bitmap)
                text(
                    """Analyze this image of a waste/garbage issue. 
                    Classify the waste type and suggest prioritized actions.
                    Return JSON: { "type": "...", "action": "...", "severity": "low" | "medium" | "high" }"""
                )
            }

            val response = visionModel.generateContent(prompt)
            val text = response.text ?: return WasteAnalysis()

            // Parse JSON from response (may contain markdown code fences)
            val jsonStr = text
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val json = JSONObject(jsonStr)
            WasteAnalysis(
                type = json.optString("type", "Unknown"),
                action = json.optString("action", "Dispose properly"),
                severity = json.optString("severity", "medium")
            )
        } catch (e: Exception) {
            WasteAnalysis(type = "Unknown", action = "AI Analysis unavailable", severity = "medium")
        }
    }

    /**
     * Classify a waste item by name (text-based).
     * Used in the Education screen's AI assistant.
     */
    suspend fun classifyWasteItem(itemName: String): WasteClassification {
        return try {
            val response = textModel.generateContent(
                """Classify which waste category this item belongs to: "$itemName". 
                Categories: Wet Waste, Dry Waste, Hazardous, Sanitary, or E-Waste.
                Provide the answer in JSON format: { "category": "...", "instruction": "..." } 
                where instruction is a short (1 sentence) disposal rule."""
            )
            val text = response.text ?: return WasteClassification()

            val jsonStr = text
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val json = JSONObject(jsonStr)
            WasteClassification(
                category = json.optString("category", "Unknown"),
                instruction = json.optString("instruction", "Please consult local guidelines.")
            )
        } catch (e: Exception) {
            WasteClassification(category = "Unknown", instruction = "Classification unavailable.")
        }
    }

    /**
     * Generate an executive summary for the Admin dashboard based on report descriptions.
     */
    suspend fun generateExecutiveSummary(reportDescriptions: List<String>): String {
        return try {
            val reportList = reportDescriptions.joinToString("\n") { "- $it" }
            val response = textModel.generateContent(
                """Provide a high-level, executive summary for the Village Panchayat based on these reports:
                $reportList
                Format: 2 sentences max. Focused on status, primary waste issues, and recommendation for vehicle deployment."""
            )
            response.text ?: "No summary available."
        } catch (e: Exception) {
            "Summary generation failed. Please try again."
        }
    }

    companion object {
        private const val FALLBACK_INSIGHT = "Keep your village clean, start composting today!"
    }
}
