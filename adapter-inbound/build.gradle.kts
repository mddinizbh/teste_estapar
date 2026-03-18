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

    implementation("io.micronaut:micronaut-http-server-netty")
    implementation("io.micronaut.serde:micronaut-serde-jackson")

    ksp("io.micronaut:micronaut-http-validation")
    ksp("io.micronaut.serde:micronaut-serde-processor")
}
