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
    id("com.android.library")
    kotlin("multiplatform")
    id("com.codingfeline.buildkonfig")
    id("org.jetbrains.compose")
    id("de.comahe.i18n4k")
    id("dev.icerock.mobile.multiplatform-resources")
    id("org.jetbrains.kotlin.plugin.atomicfu")
    kotlin("plugin.serialization")
    id("com.bugsnag.android.gradle")
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
                jvmTarget = rootProject.extra["javaVerionEnum"].toString()
            }
        }
    }

    jvm("jvm") {
        compilations.all {
            kotlinOptions.jvmTarget = rootProject.extra["javaVersionEnum"].toString()
        }
    }

    sourceSets {
        val korlibsVersion = "4.0.10"
        val ktorVersion = "2.3.6"
        val jsoupVersion = "1.16.2"
        val coroutinesVersion = "1.7.3"
        val fluidVersion = "0.13.0"
        val settingsVersion = "1.1.0"

        val commonMain by getting {
            dependencies {
                api(compose.runtime)

                api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${rootProject.extra["kotlinVersion"]}")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")

                api("com.soywiz.korlibs.krypto:krypto:$korlibsVersion")
                api("com.soywiz.korlibs.korio:korio:$korlibsVersion")
                api("com.soywiz.korlibs.klock:klock:$korlibsVersion")
                api("io.ktor:ktor-client-core:$ktorVersion")
                api("io.ktor:ktor-client-auth:$ktorVersion")
                api("io.fluidsonic.i18n:fluid-i18n:$fluidVersion")
                api("io.fluidsonic.country:fluid-country:$fluidVersion")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
                api("com.russhwolf:multiplatform-settings:$settingsVersion")
                api("com.russhwolf:multiplatform-settings-no-arg:$settingsVersion")
                api("de.comahe.i18n4k:i18n4k-core:${rootProject.extra["i18n4kVersion"]}")
                api("dev.icerock.moko:resources:${rootProject.extra["mokoVersion"]}")
                api("dev.icerock.moko:resources-compose:${rootProject.extra["mokoVersion"]}")
            }
        }

        val nonWebMain by creating {
            dependsOn(commonMain)
        }

        val jvmMain by getting {
            dependsOn(nonWebMain)

            dependencies {
                api("org.jsoup:jsoup:$jsoupVersion")
                api("io.ktor:ktor-client-cio:$ktorVersion")
                api("com.formdev:flatlaf:3.2.5")
                api("io.github.vincenzopalazzo:material-ui-swing:1.1.4")
                api("de.comahe.i18n4k:i18n4k-core-jvm:${rootProject.extra["i18n4kVersion"]}")
                api("com.github.weisj:darklaf-core:3.0.2")
                api("com.bugsnag:bugsnag:3.7.1")
                api("org.slf4j:slf4j-simple:2.0.9")
            }
        }

        val androidMain by getting {
            dependsOn(nonWebMain)

            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
                api("org.jsoup:jsoup:$jsoupVersion")

                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.fragment:fragment-ktx:1.6.2")
                api("androidx.activity:activity-compose:1.8.0")
                api("androidx.core:core-ktx:1.12.0")
                api("androidx.preference:preference-ktx:1.2.1")
                api("androidx.documentfile:documentfile:1.1.0-alpha01")
                api("io.ktor:ktor-client-cio:$ktorVersion")
                api("de.comahe.i18n4k:i18n4k-core-jvm:${rootProject.extra["i18n4kVersion"]}")

                api("com.caverock:androidsvg-aar:1.4")
                api("com.bugsnag:bugsnag-android:5.31.3")
            }
        }
    }
}

android {
    val compileSdk: Int by rootProject.extra
    this.compileSdk = compileSdk

    defaultConfig {
        val minSdk: Int by rootProject.extra
        val targetSdk: Int by rootProject.extra

        this.minSdk = minSdk
        this.targetSdk = targetSdk
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

i18n4k {
    sourceCodeLocales = listOf("en", "ru_RU", "th_TH")
}

tasks.named("jvmProcessResources") {
    dependsOn(tasks.named("generateI18n4kFiles"))
}

tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

compose {
    val kotlinVersion = rootProject.extra["kotlinVersion"].toString()

    kotlinCompilerPlugin.set("org.jetbrains.compose.compiler:compiler:${rootProject.extra["composeCompilerVersion"]}")
    kotlinCompilerPluginArgs.add("suppressKotlinVersionCompatibilityCheck=${kotlinVersion}")
}
