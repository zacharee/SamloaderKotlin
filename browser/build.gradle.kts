import org.jetbrains.compose.compose

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = rootProject.extra["groupName"].toString()
version = rootProject.extra["versionName"].toString()

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }

    sourceSets {
        named("jsMain") {
            dependencies {
                implementation(project(":common"))
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.material)
            }
        }
    }
}
