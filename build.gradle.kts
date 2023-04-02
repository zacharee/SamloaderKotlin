val versionCode by extra(28)
val versionName by extra("1.14.1")

val compileSdk by extra(33)
val targetSdk by extra(33)
val minSdk by extra(24)

val javaVersionEnum by extra(JavaVersion.VERSION_17)

val groupName by extra("tk.zwander")
val packageName by extra("tk.zwander.samsungfirmwaredownloader")
val appName by extra("Bifrost")

val skikoVersion by extra("0.7.57")
val androidComposeVersion by extra("1.4.0")

val nodeVersion by extra("16.0.0")
val webpackVersion by extra("4.10.0")

buildscript {
    val kotlinVersion by rootProject.extra("1.8.10")
    val i18n4kVersion by extra("0.5.0")
    val mokoVersion by extra("0.21.1")

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
        classpath("org.jetbrains.compose:compose-gradle-plugin:1.4.0-alpha01-dev1004")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.android.tools.build:gradle:7.4.2")
        classpath("com.codingfeline.buildkonfig:buildkonfig-gradle-plugin:0.13.3")
        classpath("dev.icerock.moko:resources-generator:$mokoVersion")
        classpath("de.comahe.i18n4k:i18n4k-gradle-plugin:$i18n4kVersion")
        classpath("com.bugsnag:bugsnag-android-gradle-plugin:7.4.0")
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
