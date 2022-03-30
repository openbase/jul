import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Base64

plugins {
    `java-library`
    `maven-publish`
    kotlin("jvm")
    signing
}

repositories {
    mavenLocal()
    mavenCentral()
}

description = "Java Utility Lib"
group = "org.openbase"
version = rootProject.file("version.txt").readText().trim()

val releaseVersion = !version.toString().endsWith("-SNAPSHOT")

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = sourceCompatibility
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    api("org.openbase:jps:[3.5,3.6-alpha)")
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.10")
    implementation("org.jetbrains.kotlin:kotlin-script-runtime:1.5.21")
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
            credentials {
                username = findProperty("MAVEN_CENTRAL_USERNAME")?.let { it as String? }
                password = findProperty("MAVEN_CENTRAL_TOKEN")?.let { it as String? }
            }
            val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("-SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
        }
    }
}

signing {

    val privateKey = findProperty("OPENBASE_GPG_PRIVATE_KEY")
        ?.let { it as String? }
        ?.let { Base64.getDecoder().decode(it) }
        ?.let { String(it) }
        ?:run {
            // Signing skipped because of missing private key.
            return@signing
        }

    val passphrase = findProperty("OPENBASE_GPG_PRIVATE_KEY_PASSPHRASE")
        ?.let { it as String? }
        ?.let { Base64.getDecoder().decode(it) }
        ?.let { String(it) }
        ?: ""

    useInMemoryPgpKeys(
        privateKey,
        passphrase
    )
    sign(publishing.publications["mavenJava"])
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}
