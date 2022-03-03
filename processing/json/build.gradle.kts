/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("org.openbase.java-conventions")
}

dependencies {
    api(project(":jul.processing"))
    api("com.fasterxml.jackson.core:jackson-core:[2.11,2.12-alpha)")
    api("com.fasterxml.jackson.core:jackson-databind:[2.11,2.12-alpha)")
    api(project(":jul.pattern"))
    api("com.googlecode.protobuf-java-format:protobuf-java-format:1.4")
}

description = "JUL Processing JSon"

java {
    withJavadocJar()
}