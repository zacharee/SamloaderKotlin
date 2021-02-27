plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "0.3.0"
    id("com.android.library")
    kotlin("plugin.serialization") version "1.4.30"
}

group = "tk.zwander"
version = "1.0"

repositories {
    google()
    mavenCentral()
    maven {
        url = uri("https://dl.bintray.com/pdvrieze/maven")
    }
    maven(url = "https://kotlin.bintray.com/kotlinx/")
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

                api("bouncycastle:bcprov-jdk16:136")
                api("org.jdom:jdom2:2.0.6")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
                api("com.github.kittinunf.fuel:fuel:2.3.1")
                api("com.github.kittinunf.fuel:fuel-coroutines:2.3.1")

                api("org.jetbrains.kotlinx:kotlinx-io-jvm:0.1.16")
                api("com.soywiz.korlibs.krypto:krypto:2.0.6")

                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.1.1")
                implementation("com.soywiz.korlibs.korio:korio:2.0.8")
                implementation("co.touchlab:stately-common:1.1.4")
                implementation("co.touchlab:stately-isolate:1.1.4-a1")
                implementation("com.ionspin.kotlin:bignum:0.2.8")
                implementation("io.ktor:ktor-client-core:1.6.0-eap-25")
                implementation("io.ktor:ktor-client-cio:1.6.0-eap-25")
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

                api("androidx.appcompat:appcompat:1.2.0")
                api("androidx.core:core-ktx:1.3.2")
            }
        }
    }
}

android {
    compileSdkVersion(29)
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(29)
    }
}