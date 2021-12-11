plugins {
    id("org.jetbrains.compose")
    id("com.android.application")
    id("kotlin-android")
}

group = "tk.zwander"
version = rootProject.extra["versionName"].toString()

repositories {
    google()
}

dependencies {
    implementation(project(":commonCompose"))

    implementation("androidx.fragment:fragment-ktx:1.4.0")
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("androidx.documentfile:documentfile:1.1.0-alpha01")
}

android {
    val compileSdk: Int by rootProject.extra
    this.compileSdk = compileSdk

    defaultConfig {
        applicationId = "tk.zwander.samsungfirmwaredownloader"
        val minSdk: Int by rootProject.extra
        val targetSdk: Int by rootProject.extra
        val versionCode: Int by rootProject.extra
        val versionName: String by rootProject.extra

        this.minSdk = minSdk
        this.targetSdk = targetSdk

        this.versionCode = versionCode
        this.versionName = versionName
    }

    buildFeatures {
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    lint {
        abortOnError = false
    }
}

//task wrapper(type: Wrapper) {
//    gradleVersion = "6.7.1"
//}

//task prepareKotlinBuildScriptModel {
//
//}
