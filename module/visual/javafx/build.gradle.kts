plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.interface"))
    api(project(":jul.extension.type.interface"))
    api(project(":jul.schedule"))
    api("org.openjfx:javafx-base:17.0.9:win")
    api("org.openjfx:javafx-fxml:17.0.9:win")
    api("org.openjfx:javafx-graphics:17.0.9:win")
    api("org.openjfx:javafx-media:17.0.9:win")
    api("org.openjfx:javafx-controls:17.0.9:win")
    api("org.openjfx:javafx-base:17.0.9:mac")
    api("org.openjfx:javafx-fxml:17.0.9:mac")
    api("org.openjfx:javafx-graphics:17.0.9:mac")
    api("org.openjfx:javafx-media:17.0.9:mac")
    api("org.openjfx:javafx-controls:17.0.9:mac")
    api("org.openjfx:javafx-base:17.0.9:linux")
    api("org.openjfx:javafx-fxml:17.0.9:linux")
    api("org.openjfx:javafx-graphics:17.0.9:linux")
    api("org.openjfx:javafx-media:17.0.9:linux")
    api("org.openjfx:javafx-controls:17.0.9:linux")
    api("org.controlsfx:controlsfx:_")
    api("de.jensd:fontawesomefx:_")
    api("com.jfoenix:jfoenix:_")
}

description = "JUL Visual JavaFX"

java {
    withJavadocJar()
}
