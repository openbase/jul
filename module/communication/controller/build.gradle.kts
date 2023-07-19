plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.communication"))
    api(project(":jul.communication.mqtt"))
    api(project(":jul.extension.type.interface"))
    api(project(":jul.extension.type.util"))
    api(project(":jul.exception"))
    api(project(":jul.extension.protobuf"))
    api(project(":jul.interface"))
    api(project(":jul.schedule"))
    api(project(":jul.pattern.controller"))
    testApi(project(":jul.communication.mqtt.test"))
}

description = "JUL Extension Controller"
