package com.grama.wastetracker.data.model

/**
 * User roles matching Firestore "role" field.
 */
enum class UserRole(val value: String) {
    CITIZEN("citizen"),
    DRIVER("driver"),
    ADMIN("admin");

    companion object {
        fun fromValue(value: String): UserRole =
            entries.firstOrNull { it.value == value } ?: CITIZEN
    }
}

/**
 * User profile stored in Firestore /users/{uid}.
 */
data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val phoneNumber: String? = null,
    val address: String? = null,
    val role: String = UserRole.CITIZEN.value,
    val createdAt: String = ""
) {
    val userRole: UserRole get() = UserRole.fromValue(role)
}

/**
 * Geographic coordinates.
 */
data class LatLng(
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

/**
 * Vehicle status enum.
 */
enum class VehicleStatus(val value: String) {
    IDLE("idle"),
    ACTIVE("active"),
    COMPLETED("completed");

    companion object {
        fun fromValue(value: String): VehicleStatus =
            entries.firstOrNull { it.value == value } ?: IDLE
    }
}

/**
 * Waste collection vehicle with real-time position.
 */
data class Vehicle(
    val id: String = "",
    val driverId: String = "",
    val location: LatLng = LatLng(),
    val status: String = VehicleStatus.IDLE.value,
    val lastUpdate: String = "",
    val vehicleNumber: String = "",
    val etaMinutes: Int? = null,
    val route: String = "",
    val sector: String = ""
)

/**
 * Report status.
 */
enum class ReportStatus(val value: String) {
    PENDING("pending"),
    INVESTIGATING("investigating"),
    RESOLVED("resolved");

    companion object {
        fun fromValue(value: String): ReportStatus =
            entries.firstOrNull { it.value == value } ?: PENDING
    }
}

/**
 * Severity level.
 */
enum class Severity(val value: String) {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high");

    companion object {
        fun fromValue(value: String): Severity =
            entries.firstOrNull { it.value == value } ?: MEDIUM
    }
}

/**
 * Unified model for General and Offender reports.
 */
data class IncidentReport(
    val reportId: String = "",
    val type: String = "general",        // "general" | "offender"
    val issueType: String = "",
    val description: String = "",
    val photoUrl: String? = null,
    val reporterUid: String = "",        // was: reporterId
    val timestamp: String = "",          // was: createdAt
    val status: String = "PENDING",
    val location: LatLng? = null,
    val aiAnalysis: String? = null
) {
    // NOTE: Field names changed from BlackspotReport v1.
    // Existing Firestore documents use createdAt/reporterId — reseed for demo.
    
    val reportStatus: ReportStatus get() = ReportStatus.fromValue(status)
}

/**
 * Weekly collection schedule entry.
 */
data class Schedule(
    val id: String = "",
    val dayOfWeek: String = "",
    val route: String = "",
    val expectedTime: String = "",
    val day: String = "",
    val wasteType: String = "",
    val time: String = ""
)

/**
 * Waste classification result from Gemini AI.
 */
data class WasteClassification(
    val category: String = "",
    val instruction: String = ""
)

/**
 * Waste image analysis result from Gemini AI.
 */
data class WasteAnalysis(
    val type: String = "",
    val action: String = "",
    val severity: String = "medium"
)

