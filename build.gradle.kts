val kotlinVersion: String by project
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
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

kotlin {
    val nativeTarget = when (System.getProperty("os.name")) {
        "Linux" -> linuxX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {
        compilations.getByName("main") {
            cinterops {
                val openssl by creating {
                    defFile(project.file("src/cinterop/openssl.def"))
                }
            }
        }

        binaries {
            executable {
                // Use system C libraries
                val sysRoot = "/"
                val libgccRoot = File("/lib/gcc/x86_64-linux-gnu/").takeIf { it.exists() }
                    ?: File("/lib/gcc/x86_64-pc-linux-gnu/")
                // Use the most recent GCC available on the host
                val libgccPath = file("${libgccRoot.absolutePath}/${libgccRoot.list()!!.last()}")
                val overriddenProperties =
                    "targetSysRoot.linux_x64=$sysRoot;libGcc.linux_x64=$libgccPath"
                val compilerArgs = "-Xoverride-konan-properties=$overriddenProperties"
                this.freeCompilerArgs += listOf(compilerArgs)
                this.entryPoint = "opstopus.deploptopus.main"
                this@binaries.findTest("debug")?.let { it.freeCompilerArgs += listOf(compilerArgs) }
            }
        }
    }
    sourceSets {
        val nativeMain by getting {
            kotlin.srcDir("src/main")
            resources.srcDir("src/main/resources")
            dependencies {
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-curl:$ktorVersion")
                implementation("io.ktor:ktor-client-logging:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-server-core:$ktorVersion")
                implementation("io.ktor:ktor-server-cio:$ktorVersion")
                implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
                implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("ch.qos.logback:logback-classic:$logbackVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
            }
        }
        val nativeTest by getting {
            kotlin.srcDir("src/test")
            resources.srcDir("src/test/resources")
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
                implementation("io.ktor:ktor-server-test-host:$ktorVersion")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-curl:$ktorVersion")
            }
        }
    }
}
