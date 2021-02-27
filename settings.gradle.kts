pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        jcenter()
        gradlePluginPortal()
        maven { url = uri("https://maven.pkg.jetbrains.space/public/p/compose/dev") }
    }
    
}
rootProject.name = "SamloaderKotlin"
include(":android")
include(":desktop")
include(":common")

