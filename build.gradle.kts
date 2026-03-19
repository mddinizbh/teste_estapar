plugins {
    kotlin("jvm") version "2.1.20" apply false
    id("com.google.devtools.ksp") version "2.1.20-1.0.32" apply false
    id("io.micronaut.application") version "4.6.2" apply false
    id("io.micronaut.library") version "4.6.2" apply false
    kotlin("plugin.allopen") version "2.1.20" apply false
    kotlin("plugin.jpa") version "2.1.20" apply false
}

subprojects {
    repositories {
        mavenCentral()
    }

    group = "com.marley.parking"
    version = "1.0.0"
}
