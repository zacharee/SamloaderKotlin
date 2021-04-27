plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "0.3.2"
    id("com.android.library")
    kotlin("plugin.serialization") version "1.4.31"
    id("com.github.gmazzo.buildconfig") version "3.0.0"
}

group = "tk.zwander"
version = project.properties["versionName"].toString()

repositories {
    google()
    mavenCentral()
    maven {
        url = uri("https://dl.bintray.com/pdvrieze/maven")
    }
    maven(url = "https://kotlin.bintray.com/kotlinx/")
}

android {
    compileSdkVersion(29)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].resources.srcDir("src/commonMain/resources")
    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(29)

        versionCode(project.properties["versionCode"].toString().toInt())
        versionName(version.toString())
    }

    configurations {
        create("androidTestApi")
        create("androidTestDebugApi")
        create("androidTestReleaseApi")
        create("testApi")
        create("testDebugApi")
        create("testReleaseApi")
    }
}

buildConfig {
    className("GradleConfig")
    buildConfigField("String", "versionName", "\"${project.properties["versionName"]}\"")
    buildConfigField("String", "versionCode", "\"${project.properties["versionCode"]}\"")
}

kotlin {
    android()

    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.ui)

                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.1.1")
                api("org.jetbrains.kotlinx:kotlinx-io-jvm:0.1.16")

                api("com.github.aakira:napier:1.5.0-alpha1")
                api("com.soywiz.korlibs.krypto:krypto:2.0.6")
                api("com.soywiz.korlibs.korio:korio:2.0.9")
                api("co.touchlab:stately-common:1.1.4")
                api("co.touchlab:stately-isolate:1.1.4-a1")
                api("io.ktor:ktor-client-core:1.5.2")
                api("io.ktor:ktor-client-cio:1.5.2")
                api("io.fluidsonic.i18n:fluid-i18n:0.9.4")
                api("io.fluidsonic.country:fluid-country:0.9.5")
            }
        }

        val desktopMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.ui)

                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
                api("org.jsoup:jsoup:1.13.1")
            }
        }

        val androidMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.ui)

                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.3")
                api("org.jsoup:jsoup:1.13.1")

                api("androidx.appcompat:appcompat:1.3.0-rc01")
                api("androidx.core:core-ktx:1.3.2")
                api("androidx.documentfile:documentfile:1.0.1")
            }
        }
    }
}