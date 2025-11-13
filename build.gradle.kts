import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.10" apply true
}

group = "online.veloraplugins"
version = "1.0-SNAPSHOT"

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "kotlin")

    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://eldonexus.de/repository/maven-public")
        maven("https://storehouse.okaeri.eu/repository/maven-public/")
        maven("https://storehouse.okaeri.eu/repository/maven-releases/")
        maven("https://repo.chojo.dev/releases")
        maven("https://repo.alessiodp.com/releases/")
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

        implementation("eu.okaeri:okaeri-configs:5.0.6")
        implementation("eu.okaeri:okaeri-configs-serdes-commons:5.0.6")
        implementation("eu.okaeri:okaeri-configs-yaml-snakeyaml:5.0.6")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "21"
    }
}

tasks.test {
    useJUnitPlatform()
}
