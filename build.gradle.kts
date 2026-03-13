plugins {
    kotlin("jvm") version "2.2.20"
    application
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