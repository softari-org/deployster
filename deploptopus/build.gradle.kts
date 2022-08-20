val ktorVersion: String by project
val logbackVersion: String by project

plugins {
    kotlin("multiplatform").version("1.7.10")
    kotlin("plugin.serialization").version("1.7.10")
}

group = "opstopus.deploptopus"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    val hostOs = System.getProperty("os.name")
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {
        binaries {
            executable {
                entryPoint = "opstopus.deploptopus.main"
            }
        }
    }
    sourceSets {
        val nativeMain by getting {
            kotlin.srcDir("src/main")
            resources.srcDir("src/main/resources")
            dependencies {
                implementation("io.ktor:ktor-server-core:$ktorVersion")
                implementation("io.ktor:ktor-server-cio:$ktorVersion")
                implementation("ch.qos.logback:logback-classic:$logbackVersion")
            }
        }
        val nativeTest by getting {
            kotlin.srcDir("src/test")
            resources.srcDir("src/test/resources")
        }
    }
}
