import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.Properties

plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.conveyor)
}

group = rootProject.extra["groupName"].toString()
version = rootProject.extra["versionName"].toString()

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = rootProject.extra["javaVersionEnum"].toString()
        }
        jvmToolchain(rootProject.extra["javaVersionEnum"].toString().toInt())
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":common"))
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
    val localPropertiesFile = rootProject.file("local.properties")

    if (localPropertiesFile.exists()) {
        localProperties.load(localPropertiesFile.reader())
    }

    application {
        buildTypes.release.proguard {
            isEnabled.set(false)
            version.set("7.4.0")
        }

        mainClass = "MainKt"
        nativeDistributions {
            modules("jdk.crypto.ec")
            modules("java.management")

            this.packageName = packageName

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
                    val macosSigningId = localProperties.getProperty("macosSigningId", null)

                    if (macosSigningId != null) {
                        sign.set(true)
                        identity.set(macosSigningId)
                    }
                }

                notarization {
                    val macosNotarizationEmail = localProperties.getProperty("macosNotarizationEmail", null)
                    val macosNotarizationPassword = localProperties.getProperty("macosNotarizationPassword", null)
                    val macosNotarizationTeamId = localProperties.getProperty("macosNotarizationTeamId", null)

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
    kotlinCompilerPlugin.set("org.jetbrains.compose.compiler:compiler:${libs.versions.compose.compiler.get()}")
    kotlinCompilerPluginArgs.add("suppressKotlinVersionCompatibilityCheck=${libs.versions.kotlin.get()}")
}

dependencies {
    // Use the configurations created by the Conveyor plugin to tell Gradle/Conveyor where to find the artifacts for each platform.
    linuxAmd64(compose.desktop.linux_x64)
    linuxAarch64(compose.desktop.linux_arm64)
    macAmd64(compose.desktop.macos_x64)
    macAarch64(compose.desktop.macos_arm64)
    windowsAmd64(compose.desktop.windows_x64)
}

// region Work around temporary Compose bugs.
configurations.all {
    attributes {
        // https://github.com/JetBrains/compose-jb/issues/1404#issuecomment-1146894731
        attribute(Attribute.of("ui", String::class.java), "awt")
    }
}

tasks.named<hydraulic.conveyor.gradle.WriteConveyorConfigTask>("writeConveyorConfig") {
    dependsOn(tasks.named("build"))

    doLast {
        val config = StringBuilder()
        config.appendLine("app.fsname = bifrost")
        config.appendLine("app.display-name = ${project.rootProject.extra["appName"]}")
        config.appendLine("app.rdns-name = ${project.rootProject.extra["packageName"]}")
        destination.get().asFile.appendText(config.toString())
    }
}
