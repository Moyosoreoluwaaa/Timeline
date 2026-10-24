import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidxRoom)
    alias(libs.plugins.googleGmsGoogleServices)
    alias(libs.plugins.googleFirebaseCrashlytics)
    kotlin("plugin.serialization") version "2.4.10"
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.uiToolingPreview)
    implementation(libs.compose.material3)
    implementation(libs.compose.foundation)
    implementation(libs.compose.materialIconsExtended)
    debugImplementation(libs.compose.uiTooling)

    // Activity & Lifecycle
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodelCompose)
    implementation(libs.androidx.lifecycle.runtimeCompose)

    // Navigation
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)

    // Room & DataStore Persistence
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.sqlite.bundled)
    implementation(libs.androidx.datastore.preferences)

    // Ktor Networking
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)

    // Firebase & Auth
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.firebase.ai)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.install.referrer)
    implementation(libs.googleid)

    // Foreground service / Notification / Overlay
    implementation(libs.androidx.media3.session)
    implementation(libs.accompanist.permissions)
    implementation(libs.accompanist.drawablepainter)
    implementation(libs.onesignal)

    // Screenshot capture & caching
    implementation(libs.coil.compose)
    implementation(libs.zoomimage.compose.coil)
    implementation(libs.androidx.work.runtime)

    // Utilities
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)

    // ML Kit OCR & Image Labeling
    implementation(libs.mlkit.text.recognition)
    implementation(libs.mlkit.image.labeling)

    // Home-screen widget (optional)
    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // Logging
    implementation(libs.kermit)
    implementation(libs.kermit.koin)

    // RevenueCat Native Android
    implementation(libs.purchases)
    implementation(libs.purchases.ui)

    // Dependency Injection (Koin)
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.testJunit)
}

android {
    namespace = "com.timeline"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.timeline_records"
        minSdk = libs.versions.android.minSdk.get().toInt()
        //noinspection OldTargetApi
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        
        val propVersionCode = project.findProperty("APP_VERSION_CODE")?.toString()?.toIntOrNull() ?: 2
        val propVersionName = project.findProperty("APP_VERSION_NAME")?.toString() ?: "1.0.1"
        
        versionCode = propVersionCode
        versionName = propVersionName
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }

    signingConfigs {
        create("release") {
            project.findProperty("RELEASE_STORE_FILE")?.let { storeFile = file(it) }
            project.findProperty("RELEASE_STORE_PASSWORD")?.let { storePassword = it as String }
            project.findProperty("RELEASE_KEY_ALIAS")?.let { keyAlias = it as String }
            project.findProperty("RELEASE_KEY_PASSWORD")?.let { keyPassword = it as String }
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
        create("benchmark") {
            initWith(buildTypes.getByName("release"))
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
            
            // Use release signing if configured, fallback to debug
            val isSigningConfigured = project.hasProperty("RELEASE_STORE_FILE") && 
                                    project.hasProperty("RELEASE_STORE_PASSWORD") &&
                                    project.hasProperty("RELEASE_KEY_ALIAS") &&
                                    project.hasProperty("RELEASE_KEY_PASSWORD")
            
            signingConfig = if (isSigningConfigured) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}
