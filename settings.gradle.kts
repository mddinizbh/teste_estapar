rootProject.name = "parking-management"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

include("domain")
include("application")
include("adapter-inbound")
include("adapter-outbound")
include("bootstrap")
