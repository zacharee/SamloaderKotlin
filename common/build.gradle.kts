import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
}

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.compose)
    alias(libs.plugins.moko.resources)
    alias(libs.plugins.kotlin.atomicfu)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.bugsnag.android)
}


group = rootProject.extra["groupName"].toString()
version = rootProject.extra["versionName"].toString()

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.RequiresOptIn")
}

kotlin {
    androidTarget {
        compilations.forEach {
            it.kotlinOptions {
                freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
                jvmTarget = rootProject.extra["javaVersionEnum"].toString()
            }
        }
    }

    jvm("jvm") {
        compilations.all {
            kotlinOptions.jvmTarget = rootProject.extra["javaVersionEnum"].toString()
        }
        jvmToolchain(rootProject.extra["javaVersionEnum"].toString().toInt())
    }

    targets.all {
        compilations.all {
            compilerOptions.configure {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                api(compose.material3)
                api(compose.ui)
                api(libs.compose.compiler)

                api(libs.kotlin)
                api(libs.kotlin.reflect)
                api(libs.kotlinx.coroutines)
                api(libs.kotlinx.serialization.json)

                api(libs.korlibs.korio)

                api(libs.ktor.client.auth)
                api(libs.ktor.client.core)

                api(libs.fluid.country)
                api(libs.fluid.i18n)

                api(libs.multiplatformSettings)
                api(libs.multiplatformSettings.noArg)

                api(libs.moko.mvvm.flow.compose)
                api(libs.moko.resources)
                api(libs.moko.resources.compose)
            }
        }

        val skiaMain by creating {
            dependsOn(commonMain)
        }

        val jvmMain by getting {
            dependsOn(skiaMain)

            dependencies {
                api(libs.jsoup)
                api(libs.ktor.client.cio)
                api(libs.flatlaf)
                api(libs.bugsnag.jvm)
                api(libs.slf4j)
                api(libs.jSystemThemeDetector)
                api(libs.oshi.core)
            }
        }

        val androidMain by getting {
            dependsOn(commonMain)

            dependencies {
                api(libs.kotlinx.coroutines.android)
                api(libs.jsoup)

                api(libs.ktor.client.cio)

                api(libs.androidx.activity.compose)
                api(libs.androidx.core.ktx)
                api(libs.androidx.documentfile)
                api(libs.androidx.preference.ktx)

                api(libs.google.material)

                api(libs.androidSvg)

                api(libs.bugsnag.android)
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
    }

    namespace = "tk.zwander.common"

    compileOptions {
        val javaVersionEnum: JavaVersion by rootProject.extra
        sourceCompatibility = javaVersionEnum
        targetCompatibility = javaVersionEnum
    }

    buildFeatures {
        aidl = true
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].res.srcDir(File(buildDir, "generated/moko/androidMain/res"))
}

buildkonfig {
    packageName = "tk.zwander.common"
    objectName = "GradleConfig"
    exposeObjectWithName = objectName

    defaultConfigs {
        buildConfigField(STRING, "versionName", "${rootProject.extra["versionName"]}")
        buildConfigField(STRING, "versionCode", "${rootProject.extra["versionCode"]}")
        buildConfigField(STRING, "appName", "${rootProject.extra["appName"]}")
    }
}

multiplatformResources {
    multiplatformResourcesPackage = "tk.zwander.samloaderkotlin.resources" // required
}

tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

compose {
    kotlinCompilerPlugin.set("org.jetbrains.compose.compiler:compiler:${libs.versions.compose.compiler.get()}")
    kotlinCompilerPluginArgs.add("suppressKotlinVersionCompatibilityCheck=${libs.versions.kotlin.get()}")
}
