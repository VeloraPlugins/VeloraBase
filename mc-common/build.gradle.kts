group = "online.veloraplugins"
version = "1.0-SNAPSHOT"

repositories {
    repositories {
        maven(url = "https://central.sonatype.com/repository/maven-snapshots/") {
            name = "central-snapshots"
        }
    }
}

dependencies {
    // Adventure API
    implementation("net.kyori:adventure-text-minimessage:4.25.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.25.0")

}
