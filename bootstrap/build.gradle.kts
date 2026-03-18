plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    id("io.micronaut.application")
    kotlin("plugin.allopen")
    kotlin("plugin.jpa")
}

allOpen {
    annotation("jakarta.persistence.Entity")
}

micronaut {
    runtime("netty")
    processing {
        incremental(true)
        annotations("com.marley.parking.*")
    }
}

application {
    mainClass.set("com.marley.parking.ApplicationKt")
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":application"))
    implementation(project(":adapter-inbound"))
    implementation(project(":adapter-outbound"))

    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("org.yaml:snakeyaml")

    testImplementation("io.micronaut.test:micronaut-test-kotest5")
    testImplementation("io.kotest:kotest-runner-junit5:${property("kotestVersion")}")
    testImplementation("io.kotest:kotest-assertions-core:${property("kotestVersion")}")
    testImplementation("org.testcontainers:mysql:${property("testcontainersVersion")}")
    testImplementation("org.testcontainers:testcontainers:${property("testcontainersVersion")}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
