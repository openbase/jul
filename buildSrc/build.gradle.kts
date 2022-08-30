import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Support convention plugins written in Kotlin. Convention plugins are build scripts in 'src/main' that automatically become available as plugins in the main build.
    `kotlin-dsl`
    kotlin("jvm")
}

repositories {
    // Use the plugin portal to apply community plugins in convention plugins.
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:_")
    implementation("org.jetbrains.kotlin:kotlin-reflect:_")
    implementation(Kotlin.stdlib.jdk8)
    implementation(Kotlin.scriptRuntime)
    implementation("com.adarshr:gradle-test-logger-plugin:_")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}
