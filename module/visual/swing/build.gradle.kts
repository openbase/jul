plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.schedule"))
    api("org.netbeans.external:AbsoluteLayout:_")
    api(project(":jul.extension.type.interface"))
    api("org.openjfx:javafx-swing:21.0.1:win")
    api("org.openjfx:javafx-swing:21.0.1:mac")
    api("org.openjfx:javafx-swing:21.0.1:linux")
}

description = "JUL Visual Swing"

java {
    withJavadocJar()
}
