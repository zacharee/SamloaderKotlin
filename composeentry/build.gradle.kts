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
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("dev.icerock.mobile.multiplatform-resources")
    kotlin("native.cocoapods")
}


group = rootProject.extra["groupName"].toString()
version = rootProject.extra["versionName"].toString()

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.RequiresOptIn")
}

kotlin {
    macosX64 {}
    macosArm64 {}

    cocoapods {
        summary = "IDK"
        homepage = "https://zwander.dev"
        osx.deploymentTarget = "12.0"

        framework {
            baseName = "composeentry"
            isStatic = false
        }

//        pod("HTMLReader")

//        pod("HTMLKit") {
//            version = "~> 4.2"
//        }
//        useLibraries()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":commonCompose"))
            }
        }

        val macosMain by creating {
            dependsOn(commonMain)
        }

        val macosArm64Main by getting {
            dependsOn(macosMain)
        }

        val macosX64Main by getting {
            dependsOn(macosMain)
        }
    }
}

multiplatformResources {
    multiplatformResourcesPackage = "tk.zwander.samloaderkotlin.resources.entry" // required
}

tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
