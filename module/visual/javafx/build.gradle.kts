plugins {
    id("org.openbase.jul")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

javafx {
    version = "21.0.1"
    modules = listOf(
        "javafx.base",
        "javafx.graphics",
        "javafx.media",
        "javafx.controls",
        "javafx.fxml"
    )
}

dependencies {
    api(project(":jul.interface"))
    api(project(":jul.extension.type.interface"))
    api(project(":jul.schedule"))
    api("org.controlsfx:controlsfx:_")
    api("de.jensd:fontawesomefx:_")
    api("com.jfoenix:jfoenix:_")
}

description = "JUL Visual JavaFX"

java {
    withJavadocJar()
}
