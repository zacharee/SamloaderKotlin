buildscript {
    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.16.3")
    }
}

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.github.gmazzo.buildconfig") version "3.0.2"
}

group = "tk.zwander"
version = project.properties["versionName"].toString()

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin.sourceSets.all {
    languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
}

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }

    android() {
        compilations.forEach {
            it.kotlinOptions {
                freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
            }
        }
    }

    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }

    sourceSets {
        named("commonMain") {
            dependencies {
                api(compose.runtime)

                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.3.0")
//                api("org.jetbrains.kotlinx:kotlinx-io:0.1.16")
                api("com.squareup.okio:okio-multiplatform:3.0.0-alpha.9")

                api("com.soywiz.korlibs.krypto:krypto:2.4.6")
                api("com.soywiz.korlibs.korio:korio:2.4.6")
                api("com.soywiz.korlibs.klock:klock:2.4.6")
                api("co.touchlab:stately-common:1.2.0-nmm")
                api("co.touchlab:stately-isolate:1.2.0-nmm")
                api("io.ktor:ktor-client-core:1.6.4")
                api("io.ktor:ktor-client-auth:1.6.4")
                api("io.fluidsonic.i18n:fluid-i18n:0.10.0")
                api("io.fluidsonic.country:fluid-country:0.10.0")
            }
        }

        named("desktopMain") {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
                api("org.jsoup:jsoup:1.14.3")
                api("io.ktor:ktor-client-cio:1.6.4")
            }
        }

        named("androidMain") {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")
                api("org.jsoup:jsoup:1.14.3")

                api("androidx.appcompat:appcompat:1.4.0-rc01")
                api("androidx.core:core-ktx:1.7.0")
                api("androidx.documentfile:documentfile:1.1.0-alpha01")
                api("io.ktor:ktor-client-cio:1.6.4")
            }
        }

        named("jsMain") {
            dependencies {
                api(compose.web.core)
                api(compose.web.widgets)

                api("io.ktor:ktor-client-js:1.6.4")

                api("org.jetbrains.kotlin-wrappers:kotlin-react:17.0.2-pre.258-kotlin-1.5.31")
                api("org.jetbrains.kotlin-wrappers:kotlin-react-dom:17.0.2-pre.258-kotlin-1.5.31")
                api("org.jetbrains.kotlin-wrappers:kotlin-styled:5.3.1-pre.258-kotlin-1.5.31")
                api(npm("react", "17.0.2"))
                api(npm("react-dom", "17.0.2"))
                api(npm("react-bootstrap", "2.0.0-beta.5"))
                api(npm("bootstrap", "5.1.0"))
                api(npm("jquery", "3.6.0"))
                api(npm("streamsaver", "2.0.5"))
            }
        }
    }
}

android {
    compileSdk = 31

    defaultConfig {
        minSdk = 24
        targetSdk = 31
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].resources.srcDir("src/commonMain/resources")
}

buildConfig {
    className("GradleConfig")
    buildConfigField("String", "versionName", "\"${project.properties["versionName"]}\"")
    buildConfigField("String", "versionCode", "\"${project.properties["versionCode"]}\"")
}

apply(plugin = "kotlinx-atomicfu")
