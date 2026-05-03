// Top-level build file for Grama-Waste-Tracker Android
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.services) apply false
}

// Redirect build directory to a location outside of OneDrive to avoid file locking issues
allprojects {
    val buildRoot = "C:/android-builds/${rootProject.name}"
    layout.buildDirectory.set(file("$buildRoot/${project.name}"))
}
