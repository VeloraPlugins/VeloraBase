plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "VeloraBase"
include("core")
include("paper")
include("common")
include("example-paper")
