plugins {
    kotlin("jvm") version "2.2.20"
    application
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
}

application {
    mainClass.set("org.LSOffice.MainKt")
}

group = "org.LSOffice"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}
