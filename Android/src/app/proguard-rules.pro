# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ===================================
# Firebase Crashlytics
# ===================================

# Keep Crashlytics classes
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# Keep line numbers for readable stack traces
-keepattributes SourceFile,LineNumberTable

# Keep custom exception classes
-keep public class * extends java.lang.Exception

# Rename file source to "SourceFile" to make stack traces more readable
-renamesourcefileattribute SourceFile

# ===================================
# Retrofit + Gson
# ===================================

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
# Keep only Gson-serialized model classes (not entire data package)
-keep class ai.ondevice.app.data.ModelAllowlist { *; }
-keep class ai.ondevice.app.data.AllowedModel { *; }
-keep class ai.ondevice.app.data.DefaultConfig { *; }
-keep class ai.ondevice.app.data.Model { *; }
-keep class ai.ondevice.app.data.Task { *; }
-keep class ai.ondevice.app.data.ExtraDataFile { *; }

# ===================================
# Hilt / Dagger
# ===================================

-dontwarn dagger.hilt.android.internal.**
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# ===================================
# AppAuth (OpenID Connect)
# ===================================

-keep class net.openid.appauth.** { *; }
-dontwarn net.openid.appauth.**

# ===================================
# Protobuf Lite
# ===================================

-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }
-keep,allowobfuscation,allowshrinking class com.google.protobuf.** { *; }

# ===================================
# MediaPipe / LiteRT
# ===================================

-keep class com.google.mediapipe.** { *; }
-keep class org.tensorflow.** { *; }
-dontwarn com.google.mediapipe.**
-dontwarn org.tensorflow.**

# ===================================
# Kotlin Serialization
# ===================================

-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class ai.ondevice.app.**$$serializer { *; }
-keepclassmembers class ai.ondevice.app.** {
    *** Companion;
}
-keepclasseswithmembers class ai.ondevice.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ===================================
# Room Database
# ===================================

-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ===================================
# SnakeYAML
# ===================================

-keep class org.yaml.snakeyaml.** { *; }
-dontwarn org.yaml.snakeyaml.**

# ===================================
# General
# ===================================

# Keep Compose
-dontwarn androidx.compose.**

# Keep Kotlin metadata
-keepattributes RuntimeVisibleAnnotations
-keep class kotlin.Metadata { *; }

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
