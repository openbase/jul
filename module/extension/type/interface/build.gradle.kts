import org.gradle.internal.impldep.org.eclipse.jgit.lib.ObjectChecker.type

/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.exception"))
    api("org.openbase:type:_")
}

description = "JUL Extension Type Interface"

java {
    withJavadocJar()
}
