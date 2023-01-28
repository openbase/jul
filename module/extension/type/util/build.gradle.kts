plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.extension.protobuf"))
    api(project(":jul.extension.type.interface"))
    api(project(":jul.schedule"))
    api("org.openbase:type:_")
}

description = "JUL Extension RST Util"

java {
    withJavadocJar()
}
