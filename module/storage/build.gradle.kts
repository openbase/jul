plugins {
    id("org.openbase.jul")
}

dependencies {
    api(project(":jul.exception"))
    api(project(":jul.interface"))
    api(project(":jul.extension.protobuf"))
    api(project(":jul.processing"))
    api(project(":jul.schedule"))
    api("org.eclipse.jgit:org.eclipse.jgit:[5.1.7,5.1.8-alpha)")
    api("uk.com.robust-it:cloning:_")
    api(project(":jul.pattern.controller"))
}

description = "JUL Storage"
