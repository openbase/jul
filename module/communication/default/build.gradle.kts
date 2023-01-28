plugins {
    id("org.openbase.jul")
}

dependencies {
    api("org.openbase:type:_")
    api(project(":jul.extension.type.processing"))
    api(project(":jul.interface"))
    api(project(":jul.exception"))
    api("org.jetbrains.kotlin:kotlin-reflect:_")
}

description = "JUL Communication Default"

java {
    withJavadocJar()
}
