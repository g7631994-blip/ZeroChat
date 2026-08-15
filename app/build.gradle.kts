import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists())load(file.inputStream())
}

android {
    namespace = "com.zeroclone.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.zeroclone.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0-zero-noroot"
    }

    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.8" }

    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }

    signingConfigs {
        create("release") {
            storeFile = file(
                System.getenv("KEYSTORE_PATH")
                    ?: localProperties.getProperty("KEYSTORE_PATH")
                    ?: "keystore.jks"
            )
            storePassword = System.getenv("KEY_PASSWORD")
                ?: localProperties.getProperty("KEY_PASSWORD")
                ?: ""
            keyAlias = System.getenv("KEY_ALIAS")
                ?: localProperties.getProperty("KEY_ALIAS")
                ?: ""
            keyPassword = System.getenv("KEY_PASSWORD")
                ?: localProperties.getProperty("KEY_PASSWORD")
                ?: ""
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.02.00")
    implementation(composeBom)
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.8

