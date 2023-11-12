import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.Properties

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = rootProject.extra["groupName"].toString()
version = rootProject.extra["versionName"].toString()

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = rootProject.extra["javaVersionEnum"].toString()
        }
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":commonCompose"))

                implementation(compose.desktop.currentOs)
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = rootProject.extra["javaVersionEnum"].toString()
}

tasks.withType<org.gradle.jvm.tasks.Jar> {
    exclude("META-INF/*.RSA", "META-INF/*.DSA", "META-INF/*.SF")
}

compose.desktop {
    val packageName: String by rootProject.extra
    val appName: String by rootProject.extra

    val localProperties = Properties()
    localProperties.load(rootProject.file("local.properties").reader())

    application {
        buildTypes.release.proguard {
            isEnabled.set(false)
            version.set("7.4.0")
        }

        mainClass = "MainKt"
        nativeDistributions {
            modules("jdk.crypto.ec")
            modules("java.management")

            windows {
                menu = true
                this.console = true

                iconFile.set(project.file("src/jvmMain/resources/icon.ico"))
                targetFormats(TargetFormat.Exe, TargetFormat.AppImage)
            }

            macOS {
                bundleID = packageName
                iconFile.set(project.file("src/jvmMain/resources/icon.icns"))
                packageVersion = "1." + rootProject.extra["versionCode"]
                targetFormats(TargetFormat.Dmg, TargetFormat.Pkg)
                this.packageName = appName

                signing {
                    val macosSigningId: String? by localProperties

                    if (macosSigningId != null) {
                        sign.set(macosSigningId != null)
                        identity.set(macosSigningId)
                    }
                }

                notarization {
                    val macosNotarizationEmail: String? by localProperties
                    val macosNotarizationPassword: String? by localProperties
                    val macosNotarizationTeamId: String? by localProperties

                    if (macosNotarizationEmail != null) {
                        appleID.set(macosNotarizationEmail)
                    }
                    if (macosNotarizationPassword != null) {
                        password.set(macosNotarizationPassword)
                    }
                    if (macosNotarizationTeamId != null) {
                        teamID.set(macosNotarizationTeamId)
                    }
                }
            }

            linux {
                iconFile.set(project.file("src/jvmMain/resources/icon.png"))
                packageVersion = rootProject.extra["versionCode"].toString()
                targetFormats(TargetFormat.Deb, TargetFormat.AppImage)
            }

            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            this.packageName = appName
        }
    }
}

compose {
    val kotlinVersion = rootProject.extra["kotlinVersion"].toString()

    kotlinCompilerPlugin.set("org.jetbrains.compose.compiler:compiler:${rootProject.extra["composeCompilerVersion"]}")
    kotlinCompilerPluginArgs.add("suppressKotlinVersionCompatibilityCheck=${kotlinVersion}")
}
