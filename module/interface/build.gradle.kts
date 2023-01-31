plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.exception"))
    api(project(":jul.annotation"))
}

description = "JUL Interface"

java {
    withJavadocJar()
}
