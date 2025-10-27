import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.compose.reload.gradle.ComposeHotRun
import java.util.Properties

plugins {
    alias(libs.plugins.compose)
    alias(libs.plugins.conveyor)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.hot.reload)
}

group = rootProject.extra["groupName"].toString()
version = rootProject.extra["versionName"].toString()

val javaVersionEnum: JavaVersion by rootProject.extra

kotlin {
    jvmToolchain {
        this.languageVersion.set(JavaLanguageVersion.of(javaVersionEnum.toString().toInt()))
        this.vendor.set(JvmVendorSpec.MICROSOFT)
    }

    jvm {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget = JvmTarget.fromTarget(javaVersionEnum.toString())
                }
            }
        }
    }

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":common"))

                implementation(libs.vaqua)
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(javaVersionEnum.toString()))
    }
}

tasks.withType<org.gradle.jvm.tasks.Jar> {
    exclude("META-INF/*.RSA", "META-INF/*.DSA", "META-INF/*.SF")
}

tasks.withType<ComposeHotRun>().configureEach {
    mainClass.set("MainKt")
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
            modules("jdk.accessibility")

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
                    localProperties.getProperty("macosSigningId", null)?.let {
                        sign.set(true)
                        identity.set(it)
                    }
                }

                notarization {
                    localProperties.getProperty("macosNotarizationEmail", null)?.let {
                        appleID.set(it)
                    }
                    localProperties.getProperty("macosNotarizationPassword", null)?.let {
                        password.set(it)
                    }
                    localProperties.getProperty("macosNotarizationTeamId", null)?.let {
                        teamID.set(it)
                    }
                }
            }

            linux {
                modules("jdk.security.auth")
                iconFile.set(project.file("src/jvmMain/resources/icon.png"))
                packageVersion = rootProject.extra["versionCode"].toString()
                targetFormats(TargetFormat.Deb, TargetFormat.AppImage)
            }

            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            this.packageName = appName
        }
    }
}

// region Work around temporary Compose bugs.
configurations.all {
    attributes {
        // https://github.com/JetBrains/compose-jb/issues/1404#issuecomment-1146894731
        attribute(Attribute.of("ui", String::class.java), "awt")
    }
}

project.configurations.create("desktopRuntimeClasspath") {
    extendsFrom(project.configurations.findByName("jvmRuntimeClasspath"))
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

dependencies {
    linuxAarch64(libs.compose.linux.arm64)
    linuxAmd64(libs.compose.linux.x64)
    macAarch64(libs.compose.macos.arm64)
    macAarch64(libs.vaqua)
    macAmd64(libs.compose.macos.x64)
    macAmd64(libs.vaqua)
    windowsAarch64(libs.compose.windows.arm64)
    windowsAmd64(libs.compose.windows.x64)
}
