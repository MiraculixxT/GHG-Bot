plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.6.20"
    application
}

group = "de.miraculixx"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
}

dependencies {
    implementation("net.dv8tion", "JDA", "5.0.0-beta.2")
    implementation("com.github.minndevelopment", "jda-ktx","0.10.0-beta.1")
    implementation("club.minnced","discord-webhooks","0.8.4")

    implementation("org.jetbrains.kotlinx", "kotlinx-serialization-json", "1.3.3")

    implementation("io.ktor", "ktor-client-core-jvm", "2.0.1")
    implementation("io.ktor", "ktor-client-cio", "2.0.1")

    implementation("org.slf4j", "slf4j-api", "1.7.36")
    implementation("org.slf4j", "slf4j-simple", "1.7.36")

    implementation("org.yaml", "snakeyaml", "1.21")
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