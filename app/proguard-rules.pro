# Add project specific ProGuard rules here.
# Keep Firebase model classes
-keepclassmembers class com.grama.wastetracker.data.model.** { *; }

# Keep Generative AI classes
-keep class com.google.ai.client.generativeai.** { *; }

# Coil
-dontwarn coil3.**
