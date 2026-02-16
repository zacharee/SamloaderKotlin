import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.moko.resources)
}

group = rootProject.extra["groupName"].toString()
version = rootProject.extra["versionName"].toString()

dependencies {
    implementation(project(":common"))
    coreLibraryDesugaring(libs.desugar.jdk.libs)
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

        resValue("string", "app_name", "${rootProject.extra["appName"]}")
    }

    namespace = packageName

    buildFeatures {
        compose = true
        aidl = true
        resValues = true
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
        isCoreLibraryDesugaringEnabled = true
    }

    lint {
        abortOnError = false
    }

    packaging {
        resources.excludes.add("META-INF/versions/9/previous-compilation-data.bin")
    }

    androidResources {
        @Suppress("UnstableApiUsage")
        generateLocaleConfig = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(rootProject.extra["javaVersionEnum"].toString()))
    }
}

multiplatformResources {
    resourcesPackage.set("tk.zwander.samloaderkotlin.android")
}

afterEvaluate {
    base {
        archivesName.set("bifrost_android_${android.defaultConfig.versionName}")
    }
}
