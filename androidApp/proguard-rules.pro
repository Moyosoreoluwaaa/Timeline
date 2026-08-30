# Standard Android R8/ProGuard rules for Timeline

# Optimization: Keep line number information for better stack traces in crash reporting tools (e.g. Sentry/Crashlytics)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Optimization: Keep all annotations for runtime inspection (used by many libraries)
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Koin: Dependency Injection
# Most Koin rules are bundled, but we ensure generic parameter signatures are kept for reflection-based DI
-keepclassmembers class * {
    @org.koin.core.annotation.KoinInternalApi *;
}

# Kermit: Logging
# Ensure Kermit doesn't get stripped away or its tags renamed if you rely on them for log filtering
-keep class co.touchlab.kermit.* { *; }

# RevenueCat
# RevenueCat usually bundles its own ProGuard rules, but keeping common patterns is safe
-keep class com.revenuecat.purchases.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keep class kotlinx.coroutines.android.HandlerContext { *; }

# DataStore / Serialization
-keepclassmembers class * extends androidx.datastore.preferences.core.Preferences {
    <init>(...);
}

# Coil
-keep class io.coilkt.coil.** { *; }
