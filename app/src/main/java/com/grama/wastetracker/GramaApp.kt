package com.grama.wastetracker

import android.app.Application

class GramaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Firebase initializes automatically via google-services.json
    }
}
