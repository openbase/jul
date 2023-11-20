plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.schedule"))
    api("org.netbeans.external:AbsoluteLayout:_")
    api(project(":jul.extension.type.interface"))
    api("org.openjfx:javafx-swing:17.0.9:win")
    api("org.openjfx:javafx-swing:17.0.9:mac")
    api("org.openjfx:javafx-swing:17.0.9:linux")
}

description = "JUL Visual Swing"

java {
    withJavadocJar()
}
