import org.gradle.internal.impldep.org.bouncycastle.asn1.x509.X509ObjectIdentifiers.organization
import org.gradle.internal.impldep.org.eclipse.jgit.lib.ObjectChecker.type
import org.jetbrains.kotlin.gradle.plugin.statistics.ReportStatisticsToElasticSearch.url
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/*
 * This file was generated by the Gradle 'init' task.
 *
 * This project uses @Incubating APIs which are subject to change.
 */

plugins {
    `java-library`
    `maven-publish`
    kotlin("jvm")
    signing
}

val releaseVersion = !version.toString().endsWith("-SNAPSHOT")

repositories {
    mavenLocal()

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }

    maven {
        url = uri("https://bits.netbeans.org/maven2/")
    }
}

description = "Java Utility Lib"
group = "org.openbase"
version = "3.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = sourceCompatibility
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    api("org.openbase:jps:[3.5,3.6-alpha)")
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.10")
    testImplementation("org.junit.jupiter:junit-jupiter-api:[5.8,5.9-alpha)")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter:[5.8,5.9-alpha)")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "jul"
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set(rootProject.name)
                description.set("Java Utility Lib")
                url.set("https://github.com/openbase/jul/wiki")
                inceptionYear.set("2015")
                organization {
                    name.set("openbase.org")
                    url.set("https://openbase.org")
                }
                licenses {
                    license {
                        name.set("LGPLv3")
                        url.set("https://www.gnu.org/licenses/lgpl.html")
                    }
                }
                developers {
                    developer {
                        id.set("DivineThreepwood")
                        name.set("Marian Pohling")
                        email.set("divine@openbase.org")
                        url.set("https://github.com/DivineThreepwood")
                        organizationUrl.set("https://github.com/openbase")
                        organization.set("openbase.org")
                        roles.set(listOf("architect", "developer"))
                        timezone.set("+1")
                    }
                    developer {
                        id.set("pLeminoq")
                        name.set("Tamino Huxohl")
                        email.set("pleminoq@openbase.org")
                        url.set("https://github.com/pLeminoq")
                        organizationUrl.set("https://github.com/openbase")
                        organization.set("openbase.org")
                        roles.set(listOf("architect", "developer"))
                        timezone.set("+1")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/openbase/jul.git")
                    developerConnection.set("scm:git:https://github.com/openbase/jul.git")
                    url.set("https://github.com/openbase/jul.git")
                }
            }
        }
    }
    repositories {
        maven {
            name = "SonatypeOSS"
//            credentials {
//                username = if (project.hasProperty("ossrhUsername")) (project.property("ossrhUsername") as String) else "N/A"
//                password = if (project.hasProperty("ossrhPassword")) (project.property("ossrhPassword") as String) else "N/A"
//            }
            // change URLs to point to your repos, e.g. http://my.org/repo
            val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        }
    }
}

signing {
//    sign(publishing.publications)
//    sign(configurations.archives.get())

    val privateKey = System.getenv("MAVEN_GPG_PRIVATE_KEY")
    val ownertrust = System.getenv("MAVEN_GPG_OWNERTRUST")
//    println("key[$privateKey] ownertrust[$ownertrust]")
    println("user[${System.getenv("MAVEN_CENTRAL_USERNAME").subSequence(0..3)}]")

    useInMemoryPgpKeys(
        privateKey,
        ownertrust
    )
    sign(publishing.publications["mavenJava"])
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

