val versionCode by extra(20)
val versionName by extra("1.0.7")

val compileSdk by extra(31)
val targetSdk by extra(31)
val minSdk by extra(24)

val javaVersionEnum by extra(JavaVersion.VERSION_11)

val groupName by extra("tk.zwander")
val packageName by extra("tk.zwander.samsungfirmwaredownloader")
val appName by extra("Bifrost")

buildscript {
    val kotlinVersion by rootProject.extra("1.6.21")

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
        classpath("org.jetbrains.compose:compose-gradle-plugin:1.2.0-alpha01-dev679")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
        classpath("com.android.tools.build:gradle:7.3.0-alpha09")
        classpath("com.codingfeline.buildkonfig:buildkonfig-gradle-plugin:0.11.0")
        classpath("org.jetbrains.kotlin:kotlin-serialization:$kotlinVersion")
    }
}

rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().nodeVersion = "16.0.0"
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
}
