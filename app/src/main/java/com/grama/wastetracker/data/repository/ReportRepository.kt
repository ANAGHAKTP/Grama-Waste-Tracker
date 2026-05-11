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
 * Repository for Incident Report CRUD operations.
 */
class ReportRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    fun observeReports(): Flow<List<IncidentReport>> = callbackFlow {
        val listener = db.collection("reports")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val reports = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(IncidentReport::class.java)?.copy(reportId = doc.id)
                } ?: emptyList()
                trySend(reports)
            }
        awaitClose { listener.remove() }
    }

    suspend fun submitReport(
        imageUri: Uri?,
        description: String,
        aiAnalysis: String?,
        location: LatLng?,
        type: String = "general",
        issueType: String = ""
    ) {
        val uid = auth.currentUser?.uid ?: "anonymous"

        var photoUrl: String? = null
        if (imageUri != null && imageUri != Uri.EMPTY) {
            val timestamp = System.currentTimeMillis()
            val storageRef = storage.reference.child("reports/${timestamp}_report.jpg")
            storageRef.putFile(imageUri).await()
            photoUrl = storageRef.downloadUrl.await().toString()
        }

        val report = hashMapOf(
            "reporterUid" to uid,
            "photoUrl" to photoUrl,
            "description" to description,
            "aiAnalysis" to aiAnalysis,
            "status" to "PENDING",
            "type" to type,
            "issueType" to issueType,
            "location" to location?.let { hashMapOf("lat" to it.lat, "lng" to it.lng) },
            "timestamp" to Instant.now().toString()
        )
        db.collection("reports").add(report).await()
    }

    fun getCurrentUserId(): String = auth.currentUser?.uid ?: "anonymous"

    suspend fun resolveReport(reportId: String) {
        db.collection("reports").document(reportId).update("status", "RESOLVED").await()
    }
}
