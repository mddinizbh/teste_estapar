plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    id("io.micronaut.library")
    kotlin("plugin.allopen")
}

allOpen {
    annotation("jakarta.inject.Singleton")
}

micronaut {
    processing {
        incremental(true)
        annotations("com.marley.parking.application.*")
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(kotlin("stdlib"))
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    implementation("jakarta.transaction:jakarta.transaction-api:2.0.1")

    testImplementation("io.kotest:kotest-runner-junit5:${property("kotestVersion")}")
    testImplementation("io.kotest:kotest-assertions-core:${property("kotestVersion")}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
