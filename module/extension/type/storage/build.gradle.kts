/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("org.openbase.java-conventions")
}

dependencies {
    api(project(":jul.storage"))
    api("org.openbase:type:_")
}

description = "JUL Extension Type Storage"

java {
    withJavadocJar()
}