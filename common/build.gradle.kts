plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "0.3.1"
    id("com.android.library")
    kotlin("plugin.serialization") version "1.4.30"
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
    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(29)
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

                api("com.soywiz.korlibs.korim:korim:2.0.7")
                api("com.github.aakira:napier:1.5.0-alpha1")

                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
                api("com.github.kittinunf.fuel:fuel:2.3.1")
                api("com.github.kittinunf.fuel:fuel-coroutines:2.3.1")

                api("org.jetbrains.kotlinx:kotlinx-io-jvm:0.1.16")
                api("com.soywiz.korlibs.krypto:krypto:2.0.6")

                api("org.jetbrains.kotlinx:kotlinx-datetime:0.1.1")
                api("com.soywiz.korlibs.korio:korio:2.0.8")
                api("co.touchlab:stately-common:1.1.4")
                api("co.touchlab:stately-isolate:1.1.4-a1")
                api("com.ionspin.kotlin:bignum:0.2.8")
                api("io.ktor:ktor-client-core:1.6.0-eap-25")
                api("io.ktor:ktor-client-cio:1.6.0-eap-25")
            }
        }

        val desktopMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.ui)

                api("org.jdom:jdom2:2.0.6")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
                api("com.github.kittinunf.fuel:fuel:2.3.1")
                api("com.github.kittinunf.fuel:fuel-coroutines:2.3.1")
            }
        }

        val androidMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.ui)

                api("org.jdom:jdom2:2.0.6")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
                api("com.github.kittinunf.fuel:fuel:2.3.1")
                api("com.github.kittinunf.fuel:fuel-coroutines:2.3.1")

                api("androidx.appcompat:appcompat:1.3.0-beta01")
                api("androidx.core:core-ktx:1.3.2")
            }
        }
    }
}