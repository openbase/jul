plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.interface"))
    api(project(":jul.extension.type.interface"))
    api(project(":jul.schedule"))
    api("org.openjfx:javafx-base:17:win")
    api("org.openjfx:javafx-fxml:17:win")
    api("org.openjfx:javafx-graphics:17:win")
    api("org.openjfx:javafx-media:17:win")
    api("org.openjfx:javafx-controls:17:win")
    api("org.openjfx:javafx-base:17:mac")
    api("org.openjfx:javafx-fxml:17:mac")
    api("org.openjfx:javafx-graphics:17:mac")
    api("org.openjfx:javafx-media:17:mac")
    api("org.openjfx:javafx-controls:17:mac")
    api("org.openjfx:javafx-base:17:linux")
    api("org.openjfx:javafx-fxml:17:linux")
    api("org.openjfx:javafx-graphics:17:linux")
    api("org.openjfx:javafx-media:17:linux")
    api("org.openjfx:javafx-controls:17:linux")
    api("org.controlsfx:controlsfx:[9.0,9.1-alpha)")
    api("de.jensd:fontawesomefx:[8.9,8.10-alpha)")
    api("com.jfoenix:jfoenix:[9.0,9.1-alpha)")
}

description = "JUL Visual JavaFX"

java {
    withJavadocJar()
}
