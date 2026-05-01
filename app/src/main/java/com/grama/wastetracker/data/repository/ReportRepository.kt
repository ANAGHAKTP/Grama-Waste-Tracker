package com.grama.wastetracker.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.grama.wastetracker.data.model.BlackspotReport
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
     * Observe all reports in real-time, ordered by createdAt descending.
     */
    fun observeReports(): Flow<List<BlackspotReport>> = callbackFlow {
        val query = db.collection("reports")
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val reports = snapshot?.documents?.map { doc ->
                doc.toObject(BlackspotReport::class.java)?.copy(id = doc.id)
                    ?: BlackspotReport(id = doc.id)
            } ?: emptyList()
            trySend(reports)
        }

        awaitClose { listener.remove() }
    }

    /**
     * Submit a new report:
     * 1. Upload image to Firebase Storage
     * 2. Create Firestore document
     */
    suspend fun submitReport(
        imageUri: Uri,
        description: String,
        aiAnalysis: String?,
        location: LatLng = LatLng(12.9716, 77.5946) // Default mock GPS
    ) {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Must be signed in to submit a report")

        // 1. Upload image
        val timestamp = System.currentTimeMillis()
        val storageRef = storage.reference.child("reports/${timestamp}_report.jpg")
        storageRef.putFile(imageUri).await()
        val photoUrl = storageRef.downloadUrl.await().toString()

        // 2. Create Firestore document
        val report = hashMapOf(
            "reporterId" to uid,
            "photoUrl" to photoUrl,
            "description" to description,
            "aiAnalysis" to aiAnalysis,
            "status" to "pending",
            "severity" to "medium",
            "location" to hashMapOf("lat" to location.lat, "lng" to location.lng),
            "createdAt" to Instant.now().toString()
        )
        db.collection("reports").add(report).await()
    }

    /**
     * Mark a report as resolved.
     */
    suspend fun resolveReport(reportId: String) {
        db.collection("reports").document(reportId).update(
            mapOf(
                "status" to "resolved",
                "resolvedAt" to Instant.now().toString()
            )
        ).await()
    }
}
