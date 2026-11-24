# Proguard rules for PureStatus HD
-keepattributes *Annotation*
-dontwarn java.lang.invoke.**

# Keep app code & engines
-keep class com.rvbhati.purestatus.** { *; }

# Media3 & ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# AndroidX & Compose
-dontwarn androidx.compose.**
-dontwarn androidx.**

# Coil & OkHttp
-dontwarn coil.**
-dontwarn okhttp3.**
-dontwarn okio.**
