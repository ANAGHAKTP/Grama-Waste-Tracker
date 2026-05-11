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
            // Map each document in the snapshot to a Schedule object
            snapshot.documents.mapNotNull { doc -> 
                doc.toObject(Schedule::class.java)?.copy(id = doc.id) 
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Seed sample data for demonstration.
     */
    suspend fun seedSampleData() {
        val schedules = listOf(
            Schedule(day = "MON", wasteType = "Dry Waste", time = "08:00"),
            Schedule(day = "WED", wasteType = "Wet Waste", time = "07:30"),
            Schedule(day = "FRI", wasteType = "Recyclables", time = "09:00")
        )
        schedules.forEach { db.collection("schedules").add(it).await() }
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
