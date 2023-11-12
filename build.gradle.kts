val versionCode by extra(32)
val versionName by extra("1.15.1")

val compileSdk by extra(34)
val targetSdk by extra(34)
val minSdk by extra(24)

val javaVersionEnum by extra(JavaVersion.VERSION_17)

val groupName by extra("tk.zwander")
val packageName by extra("tk.zwander.samsungfirmwaredownloader")
val appName by extra("Bifrost")

val skikoVersion by extra("0.7.87")

val nodeVersion by extra("16.0.0")
val webpackVersion by extra("4.10.0")

buildscript {
    val kotlinVersion by rootProject.extra("1.9.20")
    val composeCompilerVersion by rootProject.extra("1.5.3")
    val i18n4kVersion by extra("0.6.2")
    val mokoVersion by extra("0.23.0")

    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
        maven {
            url = uri("https://jitpack.io")
        }
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("org.jetbrains.compose:compose-gradle-plugin:1.5.10")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.android.tools.build:gradle:8.1.3")
        classpath("com.codingfeline.buildkonfig:buildkonfig-gradle-plugin:0.15.0")
        classpath("dev.icerock.moko:resources-generator:$mokoVersion")
        classpath("de.comahe.i18n4k:i18n4k-gradle-plugin:$i18n4kVersion")
        classpath("com.bugsnag:bugsnag-android-gradle-plugin:8.1.0")
        classpath("org.jetbrains.kotlin:atomicfu:$kotlinVersion")
        classpath(kotlin("serialization", version = kotlinVersion))
    }
}

rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().nodeVersion = nodeVersion
}

group = groupName
version = versionName

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
//        maven { url = uri("https://zwander.dev/maven") }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf("-Xskip-prerelease-check")
        }
    }
}
