plugins {
    kotlin("jvm")
}

group = "online.veloraplugins"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))

    implementation("eu.okaeri:okaeri-configs-yaml-bukkit:5.0.6")
    implementation("eu.okaeri:okaeri-configs-serdes-bukkit:5.0.6")

    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}