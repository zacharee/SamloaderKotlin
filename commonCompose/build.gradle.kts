plugins {
    id("com.android.library")
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.github.gmazzo.buildconfig") version "3.0.3"
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
                api(project(":common"))
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                api(compose.ui)
            }
        }

        named("androidMain") {
            dependencies {
                api(project(":common"))
            }
        }

        named("desktopMain") {
            dependencies {
                api(project(":common"))
            }
        }
    }
}

android {
    signingConfigs {
        getByName("debug") {
            storeFile = file(rootProject.)
            storePassword = rootProject.
            keyAlias = rootProject.
            keyPassword = rootProject.
        }
    }
    compileSdk = 31
    defaultConfig {
        minSdk = 30
        targetSdk = 31
        versionCode = rootProject.
        versionName = rootProject.
        versionNameSuffix = rootProject.
        signingConfig = signingConfigs.getByName("debug")
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].resources.srcDir("src/commonMain/resources")
    compileOptions {
        sourceCompatibility = rootProject.
        targetCompatibility = rootProject.
    }
    buildToolsVersion = "31.0.0"
    ndkVersion = "23.0.7599858"
    buildTypes {
        getByName("release") {
            versionNameSuffix = rootProject.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}
