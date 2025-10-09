import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("io.github.gradle-nexus.publish-plugin")
}

group = "org.openbase"

nexusPublishing {
    repositories {
        sonatype {
            username.set(findProperty("MAVEN_CENTRAL_USERNAME")?.let { it as String? })
            password.set(findProperty("MAVEN_CENTRAL_TOKEN")?.let { it as String? })
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
        }
    }
}

tasks.withType(KotlinCompile::class).all {
    kotlinOptions {
        jvmTarget = "21"
    }
}
