# 🌿 Grama-Waste Tracker

> **A Smart Village Waste Management System** powered by AI and Real-time Logistics.

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material3-green.svg)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Firebase-Auth_Firestore_Storage-orange.svg)](https://firebase.google.com/)
[![Gemini AI](https://img.shields.io/badge/AI-Google_Gemini_Flash-purple.svg)](https://deepmind.google/technologies/gemini/)

---

## 🚀 Overview

**Grama-Waste Tracker** is designed to solve the waste management crisis in rural Gram Panchayats. By providing real-time visibility into collection vehicles and leveraging AI for incident reporting, the app ensures that no "Kachara Gaadi" is missed and illegal dumping is addressed immediately.

## ✨ Key Features

### 📍 Live Vehicle Tracking
*   **Real-time Map:** Watch the collection truck move live on a custom-styled Google Map.
*   **Precision ETA:** Get accurate arrival times based on your current location.
*   **Status Alerts:** Immediate feedback when the vehicle is "In Transit" or "Arrived."

### 🤖 AI Incident Log (Incident Protocol)
*   **Photo Evidence:** Capture images of garbage piles or offenders.
*   **Automated Analysis:** Google Gemini AI analyzes the image to classify waste types and assess severity.
*   **Report Tracking:** A "My Activity" section to monitor the resolution status of your reports.

### 📚 Smart Education & Rewards
*   **AI Disposal Guide:** Ask the AI how to dispose of specific items (e.g., "Where do I put old medicine?").
*   **Visual Guidelines:** Learn the standards for Wet vs. Dry waste segregation.
*   **Gamified Progress:** Earn compliance credits for being a responsible citizen.

### 🛡️ Admin "ROOT" Terminal
*   **Real-time Feed:** Stream of community reports with AI-generated executive summaries.
*   **Logistics Management:** Seed and manage collection schedules and vehicle fleets.
*   **Resolution Engine:** Execute and track the cleanup of reported blackspots.

---

## 🛠️ Tech Stack

| Component | Technology |
|-----------|------------|
| **UI Framework** | Kotlin, Jetpack Compose, Material 3 |
| **Backend** | Firebase (Authentication, Cloud Firestore, Storage) |
| **AI Integration** | Google Generative AI (Gemini 1.5 Flash SDK) |
| **Maps** | Google Maps SDK for Android (Compose) |
| **Architecture** | MVVM (Model-View-ViewModel) + Clean Repository Pattern |

---

## ⚙️ Setup & Installation

### 1. Prerequisites
*   Android Studio (Ladybug or newer)
*   Google Maps API Key
*   Gemini API Key (via [Google AI Studio](https://aistudio.google.com/))
*   Firebase Project

### 2. Configuration
Add your keys to `local.properties`:
```properties
MAPS_API_KEY=your_maps_key
GEMINI_API_KEY=your_gemini_key
WEB_CLIENT_ID=your_firebase_web_client_id
```

### 3. Firebase Setup
1. Add `google-services.json` to the `app/` directory.
2. Enable **Email/Password** and **Google Sign-In** in Firebase Auth.
3. Create a **Firestore** database and **Storage** bucket.

---

## 📂 Project Structure

```text
app/src/main/java/com/grama/wastetracker/
├── data/
│   ├── model/         # Data classes (User, Vehicle, Report, Schedule)
│   └── repository/    # Logic for Firebase & AI interactions
├── ui/
│   ├── theme/         # Custom Premium Color & Type systems
│   ├── components/    # Reusable UI components (Cards, NavBars)
│   ├── screens/       # Main screen composables
│   └── navigation/    # Compose Navigation setup
└── viewmodel/         # State management logic
```

---

## 🤝 Contributing
Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License
This project is licensed under the MIT License.
