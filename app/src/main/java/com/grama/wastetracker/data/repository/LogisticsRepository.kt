package com.grama.wastetracker.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.grama.wastetracker.data.model.Vehicle
import com.grama.wastetracker.data.model.LatLng
import com.grama.wastetracker.data.model.Schedule
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.Instant

class LogisticsRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /**
     * Observe the nearest active vehicle in real-time.
     */
    fun observeActiveVehicle(): Flow<Vehicle?> = callbackFlow {
        val listener = db.collection("vehicles")
            .whereEqualTo("status", "active")
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Close the flow gracefully on error (e.g., permission denied during sign-out)
                    close(error)
                    return@addSnapshotListener
                }
                val vehicle = snapshot?.documents?.firstOrNull()?.toObject(Vehicle::class.java)
                trySend(vehicle)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Fetch the weekly collection schedules.
     */
    suspend fun getSchedules(): List<Schedule> {
        return try {
            val snapshot = db.collection("schedules").get().await()
            snapshot.documents.mapNotNull { doc -> 
                doc.toObject(Schedule::class.java)?.copy(id = doc.id) 
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateVehicleLocation(vehicleId: String, newLocation: LatLng) {
        db.collection("vehicles").document(vehicleId).update(
            mapOf(
                "location" to hashMapOf("lat" to newLocation.lat, "lng" to newLocation.lng),
                "lastUpdate" to Instant.now().toString()
            )
        ).await()
    }
}
