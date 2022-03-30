plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("fr.brouillard.oss.gradle.jgitver") version "0.9.1"
}

group = "org.openbase"
version = rootProject.file("version.txt").readText().trim()

nexusPublishing {
    repositories {
        sonatype {
            username.set(findProperty("MAVEN_CENTRAL_USERNAME")?.let { it as String? })
            password.set(findProperty("MAVEN_CENTRAL_TOKEN")?.let { it as String? })
        }
    }
}
