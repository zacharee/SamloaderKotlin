import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.bugsnag.gradle)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.atomicfu)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.moko.resources)
}


group = rootProject.extra["groupName"].toString()
version = rootProject.extra["versionName"].toString()

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.RequiresOptIn")
}

val javaVersionEnum: JavaVersion by rootProject.extra

kotlin {
    jvmToolchain(javaVersionEnum.toString().toInt())

    androidTarget {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.addAll("-opt-in=kotlin.RequiresOptIn", "-Xdont-warn-on-error-suppression")
                    jvmTarget = JvmTarget.fromTarget(javaVersionEnum.toString())
                }
            }
        }
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

    targets.all {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    freeCompilerArgs.addAll("-Xexpect-actual-classes", "-Xdont-warn-on-error-suppression")
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.foundation)
                api(compose.material3)
                api(compose.runtime)
                api(compose.ui)
                api(libs.material.icons.core)
                api(libs.kotlin)
                api(libs.kotlin.reflect)
                api(libs.kotlinx.coroutines)
                api(libs.kotlinx.datetime)
                api(libs.kotlinx.serialization.json)
                api(libs.ksoup)
                api(libs.ktor.client.auth)
                api(libs.ktor.client.core)
                api(libs.moko.resources)
                api(libs.moko.resources.compose)
                api(libs.multiplatformSettings)
                api(libs.multiplatformSettings.noArg)
                api(libs.richeditor.compose)
                api(libs.semver)
                api(libs.filekit.core)
                api(libs.filekit.dialogs.compose)
                api(libs.kmpfile)
                api(libs.kmpplatform)
                api(libs.zwander.composedialog)
                api(libs.zwander.materialyou)
                api(libs.csv)
                api(libs.cryptography.core)
                api(libs.kotlinx.crypto.crc32)
                api(libs.kotlin.xml.builder)
                api(libs.kotlinx.atomicfu)
            }
        }

        val androidAndJvmMain by creating {
            dependsOn(commonMain)

            dependencies {
                api(libs.ktor.client.okhttp)
                api(libs.cryptography.provider.jdk)
            }
        }

        val skiaMain by creating {
            dependsOn(commonMain)
        }

        val jvmMain by getting {
            dependsOn(androidAndJvmMain)
            dependsOn(skiaMain)

            dependencies {
                api(compose.desktop.currentOs)
                api(libs.bugsnag.jvm)
                api(libs.flatlaf)
                api(libs.jna)
                api(libs.jna.platform)
                api(libs.jsystemthemedetector)
                api(libs.kotlinx.coroutines.swing)
                api(libs.oshi.core)
                api(libs.slf4j)
                api(libs.window.styler)
                api(libs.conveyor.control)
            }
        }

        val androidMain by getting {
            dependsOn(androidAndJvmMain)

            dependencies {
                api(libs.androidx.activity.compose)
                api(libs.androidx.core.ktx)
                api(libs.androidx.documentfile)
                api(libs.androidx.preference.ktx)
                api(libs.bugsnag.android)
                api(libs.google.material)
                api(libs.kotlinx.coroutines.android)
                api(libs.github.api)
            }
        }
    }
}

android {
    val compileSdk: Int by rootProject.extra

    this.compileSdk = compileSdk

    defaultConfig {
        val minSdk: Int by rootProject.extra

        this.minSdk = minSdk

        resValue("string", "app_name", "${rootProject.extra["appName"]}")
    }

    namespace = "tk.zwander.common"

    compileOptions {
        val javaVersionEnum: JavaVersion by rootProject.extra
        sourceCompatibility = javaVersionEnum
        targetCompatibility = javaVersionEnum
        isCoreLibraryDesugaringEnabled = true
    }

    buildFeatures {
        aidl = true
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].res.srcDir(layout.buildDirectory.file("generated/moko/androidMain/res"))
}

buildkonfig {
    packageName = "tk.zwander.common"
    objectName = "GradleConfig"
    exposeObjectWithName = objectName

    defaultConfigs {
        buildConfigField(STRING, "versionName", "${rootProject.extra["versionName"]}")
        buildConfigField(STRING, "versionCode", "${rootProject.extra["versionCode"]}")
        buildConfigField(STRING, "appName", "${rootProject.extra["appName"]}")
        buildConfigField(STRING, "bugsnagJvmApiKey", "${rootProject.extra["bugsnagJvmApiKey"]}")
        buildConfigField(STRING, "bugsnagAndroidApiKey", "${rootProject.extra["bugsnagAndroidApiKey"]}")
    }
}

multiplatformResources {
    resourcesPackage.set("tk.zwander.samloaderkotlin.resources")
}

tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
}
