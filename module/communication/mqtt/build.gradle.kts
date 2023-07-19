plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.communication"))
    api(project(":jul.schedule"))
    api(project(":jul.extension.type.processing"))
    api("com.hivemq:hivemq-mqtt-client:_")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:_")
    testImplementation("org.testcontainers:junit-jupiter:_") {
        exclude(group = "junit", module = "junit")
    }
}

description = "JUL Extension MQTT"

java {
    withJavadocJar()
}
