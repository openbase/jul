plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.exception"))
}

description = "JUL Audio"

java {
    withJavadocJar()
}
