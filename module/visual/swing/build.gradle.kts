plugins {
    id("org.openbase.jul")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

javafx {
    version = "21.0.1"
    modules = listOf(
        "javafx.swing",
    )
}

dependencies {
    api(project(":jul.schedule"))
    api(project(":jul.extension.type.interface"))
    api("org.netbeans.external:AbsoluteLayout:_")
}

description = "JUL Visual Swing"

java {
    withJavadocJar()
}
