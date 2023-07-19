plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.interface"))
    api(project(":jul.schedule"))
    api(project(":jul.processing.json"))
    api("com.google.code.gson:gson:_")
    api("org.openbase:type:_")
}

description = "JUL Extension Protobuf"

java {
    withJavadocJar()
}
