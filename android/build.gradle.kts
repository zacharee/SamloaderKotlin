plugins {
    id("org.jetbrains.compose")
    id("com.android.application")
    id("kotlin-android")
    id("org.jetbrains.kotlin.android")
    id("dev.icerock.mobile.multiplatform-resources")
}

group = rootProject.extra["groupName"].toString()
version = rootProject.extra["versionName"].toString()

repositories {
    google()
}

dependencies {
    implementation(project(":common"))
}

android {
    val compileSdk: Int by rootProject.extra
    val packageName: String by rootProject.extra

    this.compileSdk = compileSdk

    defaultConfig {
        applicationId = packageName

        val minSdk: Int by rootProject.extra
        val targetSdk: Int by rootProject.extra
        val versionCode: Int by rootProject.extra
        val versionName: String by rootProject.extra

        this.minSdk = minSdk
        this.targetSdk = targetSdk

        this.versionCode = versionCode
        this.versionName = versionName
    }

    namespace = packageName

    buildFeatures {
        compose = true
        aidl = true
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
        val javaVersionEnum: JavaVersion by rootProject.extra
        sourceCompatibility = javaVersionEnum
        targetCompatibility = javaVersionEnum
    }

    kotlinOptions {
        jvmTarget = rootProject.extra["javaVersionEnum"].toString()
    }

    lint {
        abortOnError = false
    }

    packagingOptions {
        resources.excludes.add("META-INF/versions/9/previous-compilation-data.bin")
    }

    composeOptions {
        kotlinCompilerExtensionVersion = rootProject.extra["composeCompilerVersion"].toString()
    }
}

multiplatformResources {
    multiplatformResourcesPackage = "tk.zwander.samloaderkotlin.android" // required
}

compose {
    val kotlinVersion = rootProject.extra["kotlinVersion"].toString()

    kotlinCompilerPlugin.set("org.jetbrains.compose.compiler:compiler:${rootProject.extra["composeCompilerVersion"]}")
    kotlinCompilerPluginArgs.add("suppressKotlinVersionCompatibilityCheck=${kotlinVersion}")
}
