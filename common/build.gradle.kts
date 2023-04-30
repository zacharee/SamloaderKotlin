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
    kotlin("native.cocoapods")
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
    js(IR) {
        browser()
        binaries.executable()
    }

    macosX64 {}
    macosArm64 {}

    android {
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

    cocoapods {
        summary = "IDK"
        homepage = "https://zwander.dev"
        osx.deploymentTarget = "12.0"

        framework {
            baseName = "common"
            isStatic = false
        }

//        pod("HTMLReader")

//        pod("HTMLKit") {
//            version = "~> 4.2"
//        }
//        useLibraries()
    }

    sourceSets {
        val korlibsVersion = "3.4.0"
        val ktorVersion = "2.2.4"
        val jsoupVersion = "1.15.4"
        val coroutinesVersion = "1.7.0-Beta"
        val fluidVersion = "0.12.0"
        val settingsVersion = "1.0.0"

        val commonMain by getting {
            dependencies {
                api(compose.runtime)

                api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${rootProject.extra["kotlinVersion"]}")
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
//                api("com.squareup.okio:okio-multiplatform:3.0.0-alpha.9")

                api("com.soywiz.korlibs.krypto:krypto:$korlibsVersion")
                api("com.soywiz.korlibs.korio:korio:$korlibsVersion")
                api("com.soywiz.korlibs.klock:klock:$korlibsVersion")
                api("io.ktor:ktor-client-core:$ktorVersion")
                api("io.ktor:ktor-client-auth:$ktorVersion")
                api("io.fluidsonic.i18n:fluid-i18n:$fluidVersion")
                api("io.fluidsonic.country:fluid-country:$fluidVersion")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
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
                api("com.formdev:flatlaf:3.0")
                api("io.github.vincenzopalazzo:material-ui-swing:1.1.4")
                api("de.comahe.i18n4k:i18n4k-core-jvm:${rootProject.extra["i18n4kVersion"]}")
                api("com.github.weisj:darklaf-core:3.0.2")
                api("com.bugsnag:bugsnag:3.6.4")
            }
        }

        val androidMain by getting {
            dependsOn(nonWebMain)

            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
                api("org.jsoup:jsoup:$jsoupVersion")

                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.fragment:fragment-ktx:1.5.6")
                api("androidx.activity:activity-compose:1.7.0")
                api("androidx.core:core-ktx:1.9.0")
                api("androidx.documentfile:documentfile:1.1.0-alpha01")
                api("io.ktor:ktor-client-cio:$ktorVersion")
                api("de.comahe.i18n4k:i18n4k-core-jvm:${rootProject.extra["i18n4kVersion"]}")

                api("com.caverock:androidsvg-aar:1.4")
                api("com.bugsnag:bugsnag-android:5.29.0")
            }
        }

        val jsMain by getting {
            dependsOn(commonMain)

            dependencies {
                api(compose.runtime)

                api("io.ktor:ktor-client-js:$ktorVersion")
                api("de.comahe.i18n4k:i18n4k-core-js:${rootProject.extra["i18n4kVersion"]}")
                api("org.jetbrains.kotlin:kotlinx-atomicfu-runtime:1.8.20")

                api(npm("bootstrap", "5.1.0"))
                api(npm("jquery", "3.6.0"))
                api(npm("streamsaver", "2.0.5"))

//                implementation(npm("react", "18.1.0"))
//                implementation(npm("react-dom", "18.1.0"))

//                api("org.jetbrains.kotlin-wrappers:kotlin-react:18.1.0-pre.336")
//                api("org.jetbrains.kotlin-wrappers:kotlin-react-dom:18.1.0-pre.336")
//                api("org.jetbrains.kotlin-wrappers:kotlin-styled:5.3.5-pre.336")
            }
        }

        val macosMain by creating {
            dependsOn(nonWebMain)

            dependencies {
                api("com.soywiz.korlibs.korio:korio:$korlibsVersion")
                api("io.ktor:ktor-client-darwin:$ktorVersion")
            }
        }

        val macosArm64Main by getting {
            dependsOn(macosMain)
            dependencies {
                api("org.jetbrains.skiko:skiko-macosarm64:${rootProject.extra["skikoVersion"]}")
            }
        }

        val macosX64Main by getting {
            dependsOn(macosMain)
            dependencies {
                api("org.jetbrains.skiko:skiko-macosx64:${rootProject.extra["skikoVersion"]}")
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

tasks.named("jsProcessResources") {
    dependsOn(tasks.named("generateI18n4kFiles"))
}

tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

compose.experimental {
    web.application {}
}

//tasks.named<org.jetbrains.kotlin.gradle.tasks.DefFileTask>("generateDefHTMLKit").configure {
//    doLast {
//        outputFile.writeText("""
//            language = Objective-C
//            headers = HTMLKit.h
//        """.trimIndent())
//    }
//}
//
//tasks.withType<org.jetbrains.kotlin.gradle.tasks.CInteropProcess>()
//    .matching { it.name.contains("cinteropHTMLKit") }
//    .configureEach {
//        val dir = project.buildDir.resolve("cocoapods/synthetic/OSX/Pods/HTMLKit/Sources/include").absolutePath
//        settings.compilerOpts.add("-I$dir")
//    }
//
//tasks.withType<org.jetbrains.kotlin.gradle.tasks.CInteropProcess> {
//    settings.compilerOpts("-DNS_FORMAT_ARGUMENT(A)=")
//}

//apply(plugin = "kotlinx-atomicfu")
