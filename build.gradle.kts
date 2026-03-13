plugins {
    kotlin("jvm") version "2.2.20"
    application
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
    jacoco
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
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        html.required = true
    }
    // exclude Main.kt from coverage — it's a REPL, not production logic
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) { exclude("**/MainKt*.class", "**/AttrsState*.class") }
        })
    )
}
kotlin {
    jvmToolchain(23)
}
