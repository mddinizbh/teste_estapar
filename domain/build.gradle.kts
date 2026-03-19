plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")

    testImplementation("io.kotest:kotest-runner-junit5:${property("kotestVersion")}")
    testImplementation("io.kotest:kotest-assertions-core:${property("kotestVersion")}")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.5.16")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
