plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    id("io.micronaut.library")
    kotlin("plugin.allopen")
    kotlin("plugin.jpa")
}

allOpen {
    annotation("jakarta.persistence.Entity")
}

micronaut {
    processing {
        incremental(true)
        annotations("com.marley.parking.adapter.outbound.*")
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":application"))

    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")

    // Micronaut Data JPA
    implementation("io.micronaut.data:micronaut-data-hibernate-jpa")
    ksp("io.micronaut.data:micronaut-data-processor")

    // Database
    runtimeOnly("mysql:mysql-connector-java:8.0.33")
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")

    // Flyway
    implementation("io.micronaut.flyway:micronaut-flyway")
    runtimeOnly("org.flywaydb:flyway-mysql")

    // HTTP Client
    implementation("io.micronaut:micronaut-http-client")

    // Serde
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    ksp("io.micronaut.serde:micronaut-serde-processor")
}
