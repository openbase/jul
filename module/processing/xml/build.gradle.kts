plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.exception"))
    api("com.io7m.xom:xom:_")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:[2.11,2.12-alpha)")
    api("com.fasterxml.jackson.core:jackson-databind:[2.11,2.12-alpha)")
    api("com.fasterxml.jackson.core:jackson-annotations:[2.11,2.12-alpha)")
    api("com.fasterxml.jackson.core:jackson-core:[2.11,2.12-alpha)")
    api("org.codehaus.woodstox:woodstox-core-asl:[4.1,4.2-alpha)")
}

description = "JUL Processing XML"

java {
    withJavadocJar()
}
