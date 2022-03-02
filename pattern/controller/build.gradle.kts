/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("org.openbase.java-conventions")
}

dependencies {
    implementation(project(":jul.interface"))
    implementation("org.openbase:type:[1.1,1.2-alpha)")
    implementation(project(":jul.pattern"))
    implementation(project(":jul.extension.protobuf"))
}

description = "JUL Pattern Controller"

java {
    withJavadocJar()
}
