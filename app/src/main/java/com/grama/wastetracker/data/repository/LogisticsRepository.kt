package com.grama.wastetracker.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.grama.wastetracker.data.model.Schedule
import com.grama.wastetracker.data.model.Vehicle
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository for Vehicle Tracking and Collection Schedules.
 * Manages real-time vehicle updates and static schedule data.
 */
class LogisticsRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /**
     * Observe the active vehicle in real-time.
     * Used by the Dashboard and Live Map.
     */
    fun observeActiveVehicle(): Flow<Vehicle?> = callbackFlow {
        // Query for the first active vehicle
        val query = db.collection("vehicles")
            .whereEqualTo("status", "active")
            .limit(1)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            
            val vehicle = snapshot?.documents?.firstOrNull()?.let { doc ->
                doc.toObject(Vehicle::class.java)?.copy(id = doc.id)
            }
            trySend(vehicle)
        }

        awaitClose { listener.remove() }
    }

    /**
     * Fetch the weekly collection schedule from Firestore.
     */
    suspend fun getSchedules(): List<Schedule> {
        return try {
            val snapshot = db.collection("schedules")
                .get()
                .await()
            
            snapshot.documents.map { doc ->
                doc.toObject(Schedule::class.java)?.copy(id = doc.id) ?: Schedule(id = doc.id)
            }
        } catch (e: Exception) {
            // Return empty list on failure (network/permissions)
            emptyList()
        }
    }
    /**
     * Seeds sample data into Firestore for testing.
     * Only use this during development/verification.
     */
    suspend fun seedSampleData() {
        // 1. Seed Active Vehicle
        val vehicle = hashMapOf(
            "driverId" to "driver_001",
            "status" to "active",
            "vehicleNumber" to "GA-01-1234",
            "etaMinutes" to 12,
            "route" to "Market Road",
            "sector" to "Sector 04",
            "location" to hashMapOf("lat" to 12.9716, "lng" to 77.5946),
            "lastUpdate" to java.time.Instant.now().toString()
        )
        db.collection("vehicles").document("active_vehicle").set(vehicle).await()

        // 2. Seed Weekly Schedule
        val scheduleList = listOf(
            hashMapOf("day" to "MON", "wasteType" to "Dry Waste", "time" to "08:00"),
            hashMapOf("day" to "WED", "wasteType" to "Wet Waste", "time" to "07:30"),
            hashMapOf("day" to "FRI", "wasteType" to "Recyclables", "time" to "09:00")
        )

        scheduleList.forEachIndexed { index, data ->
            db.collection("schedules").document("schedule_$index").set(data).await()
        }
    }
}
