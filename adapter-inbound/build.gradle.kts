plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    id("io.micronaut.library")
}

micronaut {
    processing {
        incremental(true)
        annotations("com.marley.parking.adapter.inbound.*")
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":application"))

    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    implementation("io.micronaut:micronaut-http-server-netty")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("io.micronaut.validation:micronaut-validation")

    ksp("io.micronaut:micronaut-http-validation")
    ksp("io.micronaut.validation:micronaut-validation-processor")
    ksp("io.micronaut.serde:micronaut-serde-processor")
    ksp("io.micronaut:micronaut-inject-kotlin")
}
