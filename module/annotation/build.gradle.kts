plugins {
    id("org.openbase.jul")
}

dependencies {
    api("org.projectlombok:lombok:_")
}

description = "JUL Annoation"

java {
    withJavadocJar()
}
