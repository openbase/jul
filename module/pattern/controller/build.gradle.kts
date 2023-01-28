plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.interface"))
    api("org.openbase:type:_")
    api(project(":jul.pattern"))
    api(project(":jul.extension.protobuf"))
}

description = "JUL Pattern Controller"

java {
    withJavadocJar()
}
