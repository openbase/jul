/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("org.openbase.java-conventions")
}

dependencies {
    api(project(":jul.extension.protobuf"))
    api("org.openbase:type:[1.1,1.2-alpha)")
}

description = "JUL Extension Type Processing"

java {
    withJavadocJar()
}