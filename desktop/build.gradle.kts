import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose") version "0.3.0"
}

group = "tk.zwander"
version = "1.0.0"

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":common"))
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks.withType<org.gradle.jvm.tasks.Jar> {
    exclude("META-INF/*.RSA", "META-INF/*.DSA", "META-INF/*.SF")
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            modules("jdk.crypto.ec")

            windows {
                menu = true

                this.console = true
            }

            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe, TargetFormat.AppImage)
            packageName = "Samsung Firmware Downloader"
        }
    }
}