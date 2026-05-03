package com.grama.wastetracker.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.grama.wastetracker.data.model.IncidentReport
import com.grama.wastetracker.data.model.LatLng
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant

/**
 * Repository for Blackspot Report CRUD operations.
 * Mirrors ReportIssue.tsx and AdminDashboard.tsx Firestore logic.
 */
class ReportRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    /**
     * Observe PENDING reports in real-time, ordered by timestamp descending.
     */
    fun observeReports(): Flow<List<IncidentReport>> = callbackFlow {
        // This query requires a composite index on: status ASC, timestamp DESC
        val query = db.collection("reports")
            .whereEqualTo(ReportFields.STATUS, "PENDING")
            .orderBy(ReportFields.TIMESTAMP, Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val reports = snapshot?.documents?.map { doc ->
                doc.toObject(IncidentReport::class.java)?.copy(reportId = doc.id)
                    ?: IncidentReport(reportId = doc.id)
            } ?: emptyList()
            trySend(reports)
        }

        awaitClose { listener.remove() }
    }

    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("Must be signed in")
    }

    object ReportFields {
        const val TYPE = "type"
        const val ISSUE_TYPE = "issueType"
        const val DESCRIPTION = "description"
        const val PHOTO_URL = "photoUrl"
        const val REPORTER_UID = "reporterUid"
        const val AI_ANALYSIS = "aiAnalysis"
        const val TIMESTAMP = "timestamp"
        const val STATUS = "status"
        const val LOCATION = "location"
    }

    /**
     * Submit a new report:
     * 1. Upload image to Firebase Storage
     * 2. Create Firestore document based on ReportMode
     */
    suspend fun submitReport(
        state: com.grama.wastetracker.viewmodel.ReportState,
        issueType: String,
        reporterUid: String
    ): Result<Unit> {
        return try {
            val photoUrl = state.imageUri?.let { uri ->
                if (uri == Uri.EMPTY) return@let null
                val timestamp = System.currentTimeMillis()
                val storageRef = storage.reference.child("reports/${timestamp}_report.jpg")
                storageRef.putFile(uri).await()
                storageRef.downloadUrl.await().toString()
            }

            val document = when (state.reportMode) {
                com.grama.wastetracker.viewmodel.ReportMode.GENERAL -> hashMapOf(
                    ReportFields.TYPE to "general",
                    ReportFields.ISSUE_TYPE to issueType,
                    ReportFields.DESCRIPTION to state.description,
                    ReportFields.PHOTO_URL to photoUrl,
                    ReportFields.REPORTER_UID to reporterUid,
                    ReportFields.AI_ANALYSIS to state.aiAnalysisText,
                    ReportFields.TIMESTAMP to java.time.Instant.now().toString(),
                    ReportFields.STATUS to "PENDING",
                    // Use a mock location for now as per previous implementation
                    ReportFields.LOCATION to hashMapOf("lat" to 12.9716, "lng" to 77.5946)
                )
                com.grama.wastetracker.viewmodel.ReportMode.OFFENDER -> hashMapOf(
                    ReportFields.TYPE to "offender",
                    ReportFields.DESCRIPTION to state.description,
                    ReportFields.PHOTO_URL to photoUrl,
                    ReportFields.REPORTER_UID to reporterUid,
                    ReportFields.AI_ANALYSIS to state.aiAnalysisText,
                    ReportFields.TIMESTAMP to java.time.Instant.now().toString(),
                    ReportFields.STATUS to "PENDING",
                    ReportFields.LOCATION to hashMapOf("lat" to 12.9716, "lng" to 77.5946)
                )
            }

            db.collection("reports").add(document).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mark a report as resolved.
     */
    suspend fun resolveReport(reportId: String) {
        db.collection("reports").document(reportId).update(
            mapOf(
                ReportFields.STATUS to "RESOLVED"
            )
        ).await()
    }
}
