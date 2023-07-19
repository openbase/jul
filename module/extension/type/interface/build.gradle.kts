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
