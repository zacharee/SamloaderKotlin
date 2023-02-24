plugins {
    id("com.android.library")
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("dev.icerock.mobile.multiplatform-resources")
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

    jvm("jvm") {
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
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                api(compose.material3)
                api(compose.ui)
                api("moe.tlaster:precompose:1.4.0")
            }
        }

        val skiaMain by creating {
            dependsOn(commonMain)
        }

        val androidMain by getting {
            dependsOn(commonMain)

            dependencies {
                api(project(":common"))
                // TODO: Remove this once JB Compose updates to 1.4.x
                api("androidx.compose.foundation:foundation:${rootProject.extra["androidComposeVersion"]}")
            }
        }

        val jvmMain by getting {
            dependsOn(skiaMain)

            dependencies {
                api(project(":common"))
            }
        }

        val macosMain by creating {
            dependsOn(skiaMain)
        }

        val macosArm64Main by getting {
            dependsOn(macosMain)
        }

        val macosX64Main by getting {
            dependsOn(macosMain)
        }

        val jsMain by getting {
            dependsOn(skiaMain)
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

    compileOptions {
        val javaVersionEnum: JavaVersion by rootProject.extra
        sourceCompatibility = javaVersionEnum
        targetCompatibility = javaVersionEnum
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
}

compose.experimental {
    web.application {}
}

multiplatformResources {
    multiplatformResourcesPackage = "tk.zwander.samloaderkotlin.compose"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += listOf("-Xskip-prerelease-check")
    }
}

