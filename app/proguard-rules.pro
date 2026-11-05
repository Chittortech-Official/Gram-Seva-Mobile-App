# Proguard rules for GramSeva Mobile App (ChittorTech)

-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Kotlinx Serialization
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers @kotlinx.serialization.Serializable class * {
    *** Companion;
    *** serializer(...);
}
-keep @kotlinx.serialization.Serializable class * { *; }

# Data Models
-keep class com.chittortech.gramseva.data.models.** { *; }
-keepclassmembers class com.chittortech.gramseva.data.models.** { *; }

# Android Compose & Coroutines
-dontwarn kotlinx.coroutines.**

# OkHttp / Okio / Network
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.slf4j.**

# Kotlin metadata & reflect
-dontwarn kotlin.reflect.**
-dontwarn kotlin.Unit
