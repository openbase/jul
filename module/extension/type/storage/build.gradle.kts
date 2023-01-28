plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.storage"))
    api("org.openbase:type:_")
}

description = "JUL Extension Type Storage"

java {
    withJavadocJar()
}
