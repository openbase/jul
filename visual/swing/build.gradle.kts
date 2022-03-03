/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("org.openbase.java-conventions")
}

dependencies {
    api(project(":jul.schedule"))
    api("org.netbeans.external:AbsoluteLayout:RELEASE100")
    api(project(":jul.extension.type.interface"))
    api("org.openjfx:javafx-swing:17:win")
    api("org.openjfx:javafx-swing:17:mac")
    api("org.openjfx:javafx-swing:17:linux")
}

description = "JUL Visual Swing"

java {
    withJavadocJar()
}
