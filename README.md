<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://github.com/user-attachments/assets/0aa67016-6eaf-458a-adb2-6e31a0763ed6" />
</div>

# Grama-Waste Tracker — Android (Kotlin + Jetpack Compose)

A smart village waste management system featuring real-time vehicle tracking, AI-powered waste classification, and community reporting.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Kotlin, Jetpack Compose, Material 3 |
| Navigation | Compose Navigation |
| Auth | Firebase Auth (Google Sign-In via Credential Manager) |
| Database | Cloud Firestore (real-time listeners) |
| Storage | Firebase Storage |
| AI | Google Generative AI (Gemini) |
| Maps | Google Maps Compose |
| Images | Coil 3 |

## Setup

**Prerequisites:** Android Studio (Ladybug or newer), JDK 17+

### 1. Clone & Open
```bash
git clone https://github.com/ANAGHAKTP/Grama-Waste-Tracker.git
```
Open the project in Android Studio.

### 2. Firebase Setup
1. Go to [Firebase Console](https://console.firebase.google.com/) → your project
2. Add an Android app with package name `com.grama.wastetracker`
3. Download `google-services.json` and place it in `app/`
4. Enable **Google Sign-In** under Authentication → Sign-in method
5. Copy the **Web client ID** from the Google provider settings

### 3. API Keys
Add to `local.properties` (project root, git-ignored):
```properties
GEMINI_API_KEY=your_gemini_api_key
WEB_CLIENT_ID=your_firebase_web_client_id
MAPS_API_KEY=your_google_maps_api_key
```

### 4. Build & Run
```bash
./gradlew assembleDebug
```
Or click ▶ in Android Studio.

## Screens

| Screen | Description |
|--------|-------------|
| **Login** | Google Sign-In, auto user profile creation |
| **Dashboard** | AI daily tip, vehicle ETA, quick actions, schedule |
| **Live Map** | Google Maps with simulated vehicle tracking |
| **Report Issue** | Camera capture + AI image analysis + Firestore upload |
| **Education** | AI waste classifier, segregation guide, gamified rewards |
| **Admin** | Real-time report feed, AI summary, resolve actions |

## Architecture

```
app/src/main/java/com/grama/wastetracker/
├── data/
│   ├── model/Models.kt
│   └── repository/{Auth,Report,Gemini}Repository.kt
├── ui/
│   ├── theme/{Color,Type,Theme}.kt
│   ├── components/{BottomNavBar,GeometricCard,SectionHeader}.kt
│   ├── screens/{Login,Dashboard,LiveMap,ReportIssue,Education,AdminDashboard}Screen.kt
│   └── navigation/AppNavigation.kt
├── viewmodel/{Auth,Dashboard,Map,Report,Education,Admin}ViewModel.kt
├── MainActivity.kt
└── GramaApp.kt
```
