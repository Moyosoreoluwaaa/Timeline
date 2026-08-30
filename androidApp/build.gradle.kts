import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.uiToolingPreview)
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
}

android {
    namespace = "com.timeline"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.timeline_records"
        minSdk = libs.versions.android.minSdk.get().toInt()
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
            
            // Fallback to debug signing if release properties are not provided
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

// Safely copy and rename the bundle to the project's 'release' folder for easy access
tasks.register("renameReleaseBundle") {
    dependsOn("bundleRelease")
    
    val buildDir = layout.buildDirectory
    val vName = android.defaultConfig.versionName ?: "1.0"
    val vCode = android.defaultConfig.versionCode ?: 1
    val propsFile = project.rootProject.file("gradle.properties")
    val releaseDir = project.file("release")

    doLast {
        val fileName = "Timeline-v$vName($vCode).aab"
        val bundleFile = buildDir.dir("outputs/bundle/release/androidApp-release.aab").get().asFile
        
        if (bundleFile.exists()) {
            if (!releaseDir.exists()) releaseDir.mkdirs()
            val targetFile = File(releaseDir, fileName)
            bundleFile.copyTo(targetFile, overwrite = true)
            println("Production bundle copied to: ${targetFile.absolutePath}")
            
            // Auto-increment version code for the NEXT build
            if (propsFile.exists()) {
                val props = Properties()
                propsFile.inputStream().use { props.load(it) }
                val currentCodeStr = props.getProperty("APP_VERSION_CODE")
                val nextCode = (currentCodeStr?.toIntOrNull() ?: vCode) + 1
                props.setProperty("APP_VERSION_CODE", nextCode.toString())
                propsFile.outputStream().use { props.store(it, "Auto-incremented version code") }
                println("Version code bumped to $nextCode in gradle.properties")
            }
        } else {
            println("Error: Could not find bundle at ${bundleFile.absolutePath}")
        }
    }
}

// Automatically trigger renaming and version bumping after every release bundle build
tasks.matching { it.name == "bundleRelease" }.configureEach {
    finalizedBy("renameReleaseBundle")
}
