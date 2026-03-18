plugins {
    kotlin("jvm")
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
