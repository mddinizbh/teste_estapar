plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp")
    id("io.micronaut.library")
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

    testImplementation("io.kotest:kotest-runner-junit5:${property("kotestVersion")}")
    testImplementation("io.kotest:kotest-assertions-core:${property("kotestVersion")}")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
