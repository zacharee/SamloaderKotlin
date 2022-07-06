plugins {
    id("com.android.library")
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = rootProject.extra["groupName"].toString()
version = rootProject.extra["versionName"].toString()

repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    android {
        compilations.forEach {
            it.kotlinOptions {
                freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
            }
        }
    }

    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = rootProject.extra["javaVersionEnum"].toString()
        }
    }

    macosX64 {}

    macosArm64 {}

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":common"))
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.ui)
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.3")
//                api("com.github.zacharee:snapper:7b28eb9986")
            }
        }

        val androidMain by getting {
            dependencies {
                api(project(":common"))
                api("com.google.accompanist:accompanist-pager:0.24.12-rc")
                api("com.google.accompanist:accompanist-pager-indicators:0.24.11-rc")
            }
        }

        val desktopMain by getting {
            dependencies {
                api(project(":common"))
            }
        }

        val macosMain by creating {
            dependsOn(commonMain)
        }

        val macosArm64Main by getting {
            dependsOn(macosMain)
        }

        val macosX64Main by getting {
            dependsOn(macosMain)
        }
    }
}

//compose.experimental {
//    uikit.application {
//        bundleIdPrefix = "dev.zwander"
//        projectName = "Bifrost"
//        deployConfigurations {
//            simulator("IPhone8") {
//                //Usage: ./gradlew iosDeployIPhone8Debug
//                device = org.jetbrains.compose.experimental.dsl.IOSDevices.IPHONE_8
//            }
//            simulator("IPad") {
//                //Usage: ./gradlew iosDeployIPadDebug
//                device = org.jetbrains.compose.experimental.dsl.IOSDevices.IPAD_MINI_6th_Gen
//            }
//            connectedDevice("Device") {
//                //First need specify your teamId here, or in local.properties (compose.ios.teamId=***)
//                //teamId="***"
//                //Usage: ./gradlew iosDeployDeviceRelease
//            }
//        }
//    }
//}

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

