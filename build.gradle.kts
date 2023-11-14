val versionCode by extra(32)
val versionName by extra("1.15.1")

val compileSdk by extra(34)
val targetSdk by extra(34)
val minSdk by extra(24)

val javaVersionEnum by extra(JavaVersion.VERSION_18)

val groupName by extra("tk.zwander")
val packageName by extra("tk.zwander.samsungfirmwaredownloader")
val appName by extra("Bifrost")

buildscript {
    val kotlinVersion by rootProject.extra("1.9.20")
    val composeCompilerVersion by rootProject.extra("1.5.3")
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
        classpath("org.jetbrains.compose:compose-gradle-plugin:1.6.0-dev1265")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.android.tools.build:gradle:8.1.3")
        classpath("com.codingfeline.buildkonfig:buildkonfig-gradle-plugin:0.15.0")
        classpath("dev.icerock.moko:resources-generator:$mokoVersion")
        classpath("com.bugsnag:bugsnag-android-gradle-plugin:8.1.0")
        classpath("org.jetbrains.kotlin:atomicfu:$kotlinVersion")
        classpath(kotlin("serialization", version = kotlinVersion))
    }
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
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + listOf("-Xskip-prerelease-check")
        }
    }
}
