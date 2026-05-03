package com.grama.wastetracker

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

class GramaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Configure Firestore for better offline support
        try {
            val db = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
            db.firestoreSettings = settings
        } catch (e: Exception) {
            // Log or handle initialization error
        }
    }
}
