plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.interface"))
    api(project(":jul.communication.controller"))
    testApi(project(":jul.communication.mqtt.test"))
}

description = "JUL Pattern Launch"

java {
    withJavadocJar()
}
