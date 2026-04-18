plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
//    kotlin("android")
}

android {
    namespace = "com.example.atrackd"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.atrackd"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {

    // -----------------------------
    // Compose BOM
    // -----------------------------
    implementation(platform("androidx.compose:compose-bom:2026.03.01"))
    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.compose.foundation:foundation-layout:1.10.6")
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.03.01"))

    // -----------------------------
    // Core Android
    // -----------------------------
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")

    // -----------------------------
    // Compose
    // -----------------------------
    implementation("androidx.activity:activity-compose")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // -----------------------------
    // Lifecycle
    // -----------------------------
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")

    // -----------------------------
    // Navigation
    // -----------------------------
    implementation("androidx.navigation:navigation-compose:2.9.7")

    // -----------------------------
    // Room
    // -----------------------------
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")

    // -----------------------------
    // DataStore (для настроек и состояния таймера)
    // -----------------------------
    implementation("androidx.datastore:datastore-preferences:1.2.1")

    // -----------------------------
    // Charts
    // -----------------------------
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // -----------------------------
    // Icons
    // -----------------------------
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    // -----------------------------
    // Serialization
    // -----------------------------
    implementation("com.google.code.gson:gson:2.12.1")

    // -----------------------------
    // Additional UI
    // -----------------------------
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.36.0")

    // -----------------------------
    // Debug
    // -----------------------------
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // -----------------------------
    // Tests
    // -----------------------------
    testImplementation("junit:junit:4.13.2")

    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // -----------------------------
    // Added for color picker implementation
    // -----------------------------
    implementation("androidx.datastore:datastore-preferences:1.2.1")
}