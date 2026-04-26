plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
    application
}

group = "de.miraculixx"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
}

// Configure Kotlin/Java toolchain to target Java 17
kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("net.dv8tion:JDA:6.4.1")
    implementation("club.minnced:jda-ktx:0.14.2")
    implementation("club.minnced:discord-webhooks:0.8.4")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    val ktor = "3.4.2"
    implementation("io.ktor:ktor-client-core-jvm:$ktor")
    implementation("io.ktor:ktor-client-cio:$ktor")

    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.slf4j:slf4j-simple:1.7.36")

    implementation("org.yaml:snakeyaml:2.0")
}

application {
    mainClass.set("de.miraculixx.ghg_bot.MainKt")
}

tasks {
    jar {
        manifest {
            attributes["Main-Class"] = "de.miraculixx.mcord_event.MainKt"
        }
    }
}