plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.exception"))
    api(Testing.junit.jupiter)
    api(Testing.junit.jupiter.api)
    testImplementation(project(":jul.schedule"))
}

description = "JUL Test"

java {
    withJavadocJar()
}
