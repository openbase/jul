plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.processing"))
    api(project(":jul.extension.type.processing"))
    api("org.openbase:type:_")
}

description = "JUL Extension RST Transform"

java {
    withJavadocJar()
}
