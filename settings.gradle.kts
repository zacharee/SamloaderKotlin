@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()

        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/")
        maven("https://maven.hq.hydraulic.software")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()

        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev/")
        maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap/")
        maven("https://jitpack.io")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}

rootProject.name = "SamloaderKotlin"
include(":android")
include(":desktop")
include(":common")

include(":alertdialog")
project(":alertdialog").projectDir = File("./alertdialog")

include(":alertdialog:library")
project(":alertdialog:library").projectDir = File("./alertdialog/library")

include(":kmpfile")
project(":kmpfile").projectDir = File("./kmpfile")

include(":kmpfile:library")
project(":kmpfile:library").projectDir = File("./kmpfile/library")
