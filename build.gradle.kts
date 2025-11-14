import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val JVM_VERSION = 21

plugins {
    kotlin("jvm") version "2.1.10" apply true
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}

group = "online.veloraplugins"
version = "1.0-SNAPSHOT"

allprojects {
    version = rootProject.version
    group = rootProject.group
}

subprojects {
    afterEvaluate {
        if (name != "core") {
            tasks.matching { it.name == "shadowJar" }.configureEach {
                dependsOn(":core:shadowJar")
            }
        }
    }
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "kotlin")
    apply(plugin = "com.github.johnrengelman.shadow")

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://jitpack.io")
        maven("https://eldonexus.de/repository/maven-public")
        maven("https://storehouse.okaeri.eu/repository/maven-public/")
        maven("https://storehouse.okaeri.eu/repository/maven-releases/")
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
        implementation("eu.okaeri:okaeri-configs:5.0.6")
        implementation("eu.okaeri:okaeri-configs-serdes-commons:5.0.6")
        implementation("eu.okaeri:okaeri-configs-yaml-snakeyaml:5.0.6")

        implementation("org.mariadb.jdbc:mariadb-java-client:3.5.4")
        implementation("com.zaxxer:HikariCP:7.0.0")
        implementation("org.jetbrains.exposed:exposed-jdbc:0.51.1")
        implementation("org.jetbrains.exposed:exposed-dao:0.51.1")
        implementation("org.jetbrains.exposed:exposed-core:0.51.1")


        implementation("com.google.code.gson:gson:2.13.2")
        implementation("io.lettuce:lettuce-core:6.5.4.RELEASE")
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(JVM_VERSION))
    }

    kotlin {
        jvmToolchain(JVM_VERSION)
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = JVM_VERSION.toString()
    }

    tasks.withType<ShadowJar> {
        archiveFileName.set("${project.rootProject.name}-${project.name.capitalize()}.jar")
        archiveClassifier.set("")
        destinationDirectory.set(project.rootProject.layout.buildDirectory.dir("libs"))
    }
}