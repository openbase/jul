plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.pattern"))
    api("commons-io:commons-io:_")
    api("commons-lang:commons-lang:_")
    api("java3d:vecmath:_")
}

description = "JUL Processing Default"

java {
    withJavadocJar()
}
