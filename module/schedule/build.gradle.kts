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
