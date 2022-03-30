plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

group = "org.openbase"
version = "3.2-SNAPSHOT"
//rootProject.file("version.txt").readText().trim()

nexusPublishing {
    repositories {
        sonatype {
//            nexusUrl.set(uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/"))
//            snapshotRepositoryUrl.set(uri("https://oss.sonatype.org/content/repositories/snapshots/"))
            username.set(findProperty("MAVEN_CENTRAL_USERNAME")?.let { it as String? })
            password.set(findProperty("MAVEN_CENTRAL_TOKEN")?.let { it as String? })
        }
    }
}
