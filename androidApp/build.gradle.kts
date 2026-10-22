import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.googleGmsGoogleServices)
    alias(libs.plugins.googleFirebaseCrashlytics)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.compose.uiToolingPreview)
    implementation(libs.firebase.ai)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.install.referrer)
    implementation(libs.googleid)
    debugImplementation(libs.compose.uiTooling)

    // foreground service / notification / overlay
    implementation(libs.androidx.media3.session)
    implementation(libs.accompanist.permissions)
    implementation(libs.accompanist.drawablepainter)

    // screenshot capture & caching
    implementation(libs.coil.compose)
    implementation(libs.zoomimage.compose.coil) // optional — drop if no full-size viewer

    // navigation
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.work.runtime)
    implementation(libs.kotlinx.datetime)
    // home-screen widget — optional
    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // logging
    implementation(libs.kermit)
    implementation(libs.kermit.koin)

    // RevenueCat (Native Android components for Manifest)
    implementation(libs.purchases.android.ui)

    // dependency injection
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)

    testImplementation(libs.junit)
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

// Removed custom task hooks and file rename scripts for clean standard Gradle builds.
