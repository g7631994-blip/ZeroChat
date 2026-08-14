# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers @kotlinx.serialization.Serializable class ** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { *** Companion; }
-keep,includedescriptorclasses class com.zeroclone.app.**$$serializer { *; }
-keepclassmembers class com.zeroclone.app.** { *** Companion; }
-keepclasseswithmembers class com.zeroclone.app.** { kotlinx.serialization.KSerializer serializer(...); }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Koin
-keep class org.koin.** { *; }

# JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# SessionCredentials
-keep class com.zeroclone.app.service.SessionCredentials { *; }
