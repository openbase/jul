plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.communication"))
    api(project(":jul.schedule"))
    api(project(":jul.extension.type.processing"))
    api(project(":jul.communication.mqtt"))
    api(project(":jul.test"))
    api("com.hivemq:hivemq-mqtt-client:_")
    api("org.testcontainers:junit-jupiter:_") {
        exclude(group = "junit", module = "junit")
    }
    api("io.quarkus:quarkus-junit4-mock:_")
}

description = "JUL Extension MQTT Test"

java {
    withJavadocJar()
}
