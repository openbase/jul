plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.processing"))
    api("com.fasterxml.jackson.core:jackson-core:[2.11,2.12-alpha)")
    api("com.fasterxml.jackson.core:jackson-databind:[2.11,2.12-alpha)")
    api(project(":jul.pattern"))
    api("com.googlecode.protobuf-java-format:protobuf-java-format:_")
}

description = "JUL Processing JSON"

java {
    withJavadocJar()
}
