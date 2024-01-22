val versionCode by extra(49)
val versionName by extra("1.17.2")

val compileSdk by extra(34)
val targetSdk by extra(34)
val minSdk by extra(24)

val javaVersionEnum by extra(JavaVersion.VERSION_18)

val groupName by extra("tk.zwander")
val packageName by extra("tk.zwander.samsungfirmwaredownloader")
val appName by extra("Bifrost")

val bugsnagJvmApiKey by extra("a5b9774e86bc615c2e49a572b8313489")
val bugsnagAndroidApiKey by extra("3e0ed592029da1d5cc9b52160ef702ea")

plugins {
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.buildkonfig) apply false
    alias(libs.plugins.moko.resources) apply false
    alias(libs.plugins.kotlin.atomicfu) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.bugsnag.android) apply false
    alias(libs.plugins.conveyor) apply false
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap/") }
        maven {
            url = uri("https://jitpack.io")
        }
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf("-Xskip-prerelease-check")
        }
    }
}

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
        maven {
            url = uri("https://jitpack.io")
        }
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath(libs.bugsnag.android.gradle.plugin)
    }
}

group = groupName
version = versionName
