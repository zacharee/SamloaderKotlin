import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

buildscript {
    dependencies {
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:0.17.0")
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
}

apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

group = rootProject.extra["groupName"].toString()
version = rootProject.extra["versionName"].toString()

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.RequiresOptIn")
}

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }

    android {
        compilations.forEach {
            it.kotlinOptions {
                freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
            }
        }
    }

    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = rootProject.extra["javaVersionEnum"].toString()
        }
    }

    sourceSets {
        val korlibsVersion = "2.7.0"
        val ktorVersion = "2.0.0"
        val jsoupVersion = "1.14.3"

        named("commonMain") {
            dependencies {
                api(compose.runtime)

                api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${rootProject.extra["kotlinVersion"]}")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
                api("com.squareup.okio:okio-multiplatform:3.0.0-alpha.9")

                api("com.soywiz.korlibs.krypto:krypto:$korlibsVersion")
                api("com.soywiz.korlibs.korio:korio:$korlibsVersion")
                api("com.soywiz.korlibs.klock:klock:$korlibsVersion")
                api("co.touchlab:stately-common:1.2.1")
                api("co.touchlab:stately-isolate:1.2.1")
                api("io.ktor:ktor-client-core:$ktorVersion")
                api("io.ktor:ktor-client-auth:$ktorVersion")
                api("io.fluidsonic.i18n:fluid-i18n:0.10.0")
                api("io.fluidsonic.country:fluid-country:0.11.0")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
                api("com.github.Tlaster.KFilePicker:KFilePicker:1.0.4")
            }
        }

        named("desktopMain") {
            dependencies {
                api("org.jsoup:jsoup:$jsoupVersion")
                api("io.ktor:ktor-client-cio:$ktorVersion")
            }
        }

        named("androidMain") {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0")
                api("org.jsoup:jsoup:$jsoupVersion")

                api("androidx.appcompat:appcompat:1.4.1")
                api("androidx.fragment:fragment-ktx:1.4.1")
                api("androidx.activity:activity-compose:1.4.0")
                api("androidx.core:core-ktx:1.7.0")
                api("androidx.documentfile:documentfile:1.1.0-alpha01")
                api("io.ktor:ktor-client-cio:$ktorVersion")
            }
        }

        named("jsMain") {
            dependencies {
                api(compose.web.core)

                api("io.ktor:ktor-client-js:$ktorVersion")

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
        buildConfigField(STRING, "appName", "${rootProject.extra["appName"]}")
    }
}

apply(plugin = "kotlinx-atomicfu")
