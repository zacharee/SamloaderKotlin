plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = rootProject.extra["groupName"].toString()
version = rootProject.extra["versionName"].toString()

val resourcesDir = "$buildDir/resources/"

kotlin {
    js(IR) {
        browser()
        binaries.executable()
    }

    sourceSets {
        named("jsMain") {
            dependencies {
                implementation(project(":commonCompose"))
            }
        }
    }
}

compose.experimental {
    web.application {}
}

afterEvaluate {
    rootProject.extensions.configure<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension> {
        versions.webpackDevServer.version = "4.0.0"
        versions.webpackCli.version = "4.9.0"
        nodeVersion = "16.0.0"
    }
}
