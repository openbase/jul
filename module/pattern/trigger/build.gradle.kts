plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.interface"))
    api(project(":jul.pattern"))
    api(project(":jul.extension.type.processing"))
}

description = "JUL Pattern Trigger"

java {
    withJavadocJar()
}
