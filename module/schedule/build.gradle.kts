/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.annotation"))
    api(project(":jul.pattern"))
    api(project(":jul.exception"))
}

description = "JUL Schedule"

java {
    withJavadocJar()
}
