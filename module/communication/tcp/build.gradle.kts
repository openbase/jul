/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.processing.xml"))
    api(project(":jul.schedule"))
    api("org.apache.commons:commons-lang3:_")
}

description = "JUL Extension TCP"

java {
    withJavadocJar()
}
