plugins {
    id("com.android.library")
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.github.gmazzo.buildconfig") version "3.0.0"
}

group = "tk.zwander"
version = project.properties["versionName"].toString()

repositories {
    google()
    mavenCentral()
}

kotlin {
    android()

    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }

    sourceSets {
        named("commonMain") {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.ui)

                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.1.1")
                api("org.jetbrains.kotlinx:kotlinx-io-jvm:0.1.16")

                api("com.soywiz.korlibs.krypto:krypto:2.0.7")
                api("com.soywiz.korlibs.korio:korio:2.0.10")
                api("co.touchlab:stately-common:1.1.4")
                api("co.touchlab:stately-isolate:1.1.4-a1")
                api("io.ktor:ktor-client-core:1.5.2")
                api("io.ktor:ktor-client-cio:1.5.2")
                api("io.fluidsonic.i18n:fluid-i18n:0.9.4")
                api("io.fluidsonic.country:fluid-country:0.9.5")
            }
        }

        named("desktopMain") {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.ui)

                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
                api("org.jsoup:jsoup:1.13.1")
            }
        }

        named("androidMain") {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.ui)

                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")
                api("org.jsoup:jsoup:1.13.1")

                api("androidx.appcompat:appcompat:1.4.0-alpha02")
                api("androidx.core:core-ktx:1.5.0")
                api("androidx.documentfile:documentfile:1.0.1")
            }
        }
    }
}

android {
    compileSdk = 29

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].resources.srcDir("src/commonMain/resources")
}

buildConfig {
    className("GradleConfig")
    buildConfigField("String", "versionName", "\"${project.properties["versionName"]}\"")
    buildConfigField("String", "versionCode", "\"${project.properties["versionCode"]}\"")
}
