import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.16.3")
    }
}

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}

plugins {
    id("com.android.library")
    kotlin("multiplatform")
    id("com.codingfeline.buildkonfig")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization") version "1.5.31"
}

group = "tk.zwander"
version = rootProject.extra["versionName"].toString()

kotlin.sourceSets.all {
    languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
}

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }

    android {
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

                api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.31")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.3.1")
                api("com.squareup.okio:okio-multiplatform:3.0.0-alpha.9")

                api("com.soywiz.korlibs.krypto:krypto:2.4.8")
                api("com.soywiz.korlibs.korio:korio:2.4.8")
                api("com.soywiz.korlibs.klock:klock:2.4.8")
                api("co.touchlab:stately-common:1.2.0-nmm")
                api("co.touchlab:stately-isolate:1.2.0-nmm")
                api("io.ktor:ktor-client-core:1.6.5")
                api("io.ktor:ktor-client-auth:1.6.5")
                api("io.fluidsonic.i18n:fluid-i18n:0.10.0")
                api("io.fluidsonic.country:fluid-country:0.10.0")
//                api("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.5.31")
//                api("org.jetbrains.kotlin:kotlin-stdlib:1.5.31")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
            }
        }

        named("desktopMain") {
            dependencies {
                api("org.jsoup:jsoup:1.14.3")
                api("io.ktor:ktor-client-cio:1.6.5")
            }
        }

        named("androidMain") {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")
                api("org.jsoup:jsoup:1.14.3")

                api("androidx.appcompat:appcompat:1.4.0")
                api("androidx.core:core-ktx:1.7.0")
                api("androidx.documentfile:documentfile:1.1.0-alpha01")
                api("io.ktor:ktor-client-cio:1.6.5")
            }
        }

        named("jsMain") {
            dependencies {
                api(compose.web.core)

                api("io.ktor:ktor-client-js:1.6.5")

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
    val compileSdk: Int by rootProject.extra
    this.compileSdk = compileSdk

    defaultConfig {
        val minSdk: Int by rootProject.extra
        val targetSdk: Int by rootProject.extra

        this.minSdk = minSdk
        this.targetSdk = targetSdk
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].resources.srcDir("src/commonMain/resources")
}

buildkonfig {
    packageName = "tk.zwander.common"
    objectName = "GradleConfig"
    exposeObjectWithName = objectName

    defaultConfigs {
        buildConfigField(STRING, "versionName", "${rootProject.extra["versionName"]}")
        buildConfigField(STRING, "versionCode", "${rootProject.extra["versionCode"]}")
    }
}

apply(plugin = "kotlinx-atomicfu")
