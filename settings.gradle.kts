pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()

        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
}

rootProject.name = "SamloaderKotlin"
include(":android")
include(":desktop")
include(":browser")
include(":common")
include(":commonCompose")
include(":macos")
