plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.interface"))
}

description = "JUL Pattern Default"

java {
    withJavadocJar()
}
