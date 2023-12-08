pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()

        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
        maven("https://maven.hq.hydraulic.software")
    }
}

rootProject.name = "SamloaderKotlin"
include(":android")
include(":desktop")
include(":common")
