plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.extension.protobuf"))
    api("org.openbase:type:_")
}

description = "JUL Extension Type Processing"

java {
    withJavadocJar()
}
