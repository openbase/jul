plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.processing.xml"))
    api(project(":jul.schedule"))
    api("org.apache.commons:commons-lang3:_")
}

description = "JUL Extension TCP"

java {
    withJavadocJar()
}
