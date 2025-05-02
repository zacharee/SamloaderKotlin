val versionCode by extra(85)
val versionName by extra("1.20.1")

val compileSdk by extra(35)
val targetSdk by extra(35)
val minSdk by extra(24)

val javaVersionEnum by extra(JavaVersion.VERSION_21)

val groupName by extra("tk.zwander")
val packageName by extra("tk.zwander.samsungfirmwaredownloader")
val appName by extra("Bifrost")

val bugsnagJvmApiKey by extra("a5b9774e86bc615c2e49a572b8313489")
val bugsnagAndroidApiKey by extra("3e0ed592029da1d5cc9b52160ef702ea")

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.bugsnag.gradle) apply false
    alias(libs.plugins.buildkonfig) apply false
    alias(libs.plugins.compose) apply false
    alias(libs.plugins.conveyor) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.atomicfu) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.moko.resources) apply false
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.addAll("-Xskip-prerelease-check", "-Xdont-warn-on-error-suppression")
    }
}

group = groupName
version = versionName
