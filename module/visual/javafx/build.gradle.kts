plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.interface"))
    api(project(":jul.extension.type.interface"))
    api(project(":jul.schedule"))
    api("org.openjfx:javafx-base:21.0.1:win")
    api("org.openjfx:javafx-fxml:21.0.1:win")
    api("org.openjfx:javafx-graphics:21.0.1:win")
    api("org.openjfx:javafx-media:21.0.1:win")
    api("org.openjfx:javafx-controls:21.0.1:win")
    api("org.openjfx:javafx-base:21.0.1:mac")
    api("org.openjfx:javafx-fxml:21.0.1:mac")
    api("org.openjfx:javafx-graphics:21.0.1:mac")
    api("org.openjfx:javafx-media:21.0.1:mac")
    api("org.openjfx:javafx-controls:21.0.1:mac")
    api("org.openjfx:javafx-base:21.0.1:linux")
    api("org.openjfx:javafx-fxml:21.0.1:linux")
    api("org.openjfx:javafx-graphics:21.0.1:linux")
    api("org.openjfx:javafx-media:21.0.1:linux")
    api("org.openjfx:javafx-controls:21.0.1:linux")
    api("org.controlsfx:controlsfx:_")
    api("de.jensd:fontawesomefx:_")
    api("com.jfoenix:jfoenix:_")
}

description = "JUL Visual JavaFX"

java {
    withJavadocJar()
}
