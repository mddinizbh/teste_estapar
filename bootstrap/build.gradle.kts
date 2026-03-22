plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    id("io.micronaut.application")
    id("com.gradleup.shadow")
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

    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:8.0")
    runtimeOnly("org.yaml:snakeyaml")

    testImplementation("io.micronaut.test:micronaut-test-kotest5")
    testImplementation("io.kotest:kotest-runner-junit5:${property("kotestVersion")}")
    testImplementation("io.kotest:kotest-assertions-core:${property("kotestVersion")}")
    testImplementation("org.testcontainers:mysql:${property("testcontainersVersion")}")
    testImplementation("org.testcontainers:testcontainers:${property("testcontainersVersion")}")
    testImplementation("io.micronaut:micronaut-http-client")
    testRuntimeOnly("mysql:mysql-connector-java:8.0.33")
    testRuntimeOnly("commons-codec:commons-codec:1.17.1")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
