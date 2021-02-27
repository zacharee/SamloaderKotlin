plugins {
    id("org.jetbrains.compose") version "0.3.0"
    id("com.android.application")
    kotlin("android")
}

group = "tk.zwander"
version = "1.0"

repositories {
    google()
}

dependencies {
    implementation(project(":common"))
}

android {
    compileSdkVersion(29)
    defaultConfig {
        applicationId = "tk.zwander.samsungfirmwaredownloader"
        minSdkVersion(24)
        targetSdkVersion(29)
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}