group = "online.veloraplugins"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://repo.extendedclip.com/content/repositories/placeholderapi")
}

dependencies {
    // Project modules
    api(project(":core"))

    // Configuration (Okaeri)
    implementation("eu.okaeri:okaeri-configs-yaml-bukkit:5.0.6")
    implementation("eu.okaeri:okaeri-configs-serdes-bukkit:5.0.6")

    // Utility libraries
    api("com.github.cryptomorin:XSeries:13.5.1")

    // Paper platform dependencies
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.6")

    // MCCoroutine (Paper)
    compileOnly("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.22.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.22.0")
}