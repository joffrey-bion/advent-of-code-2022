plugins {
    kotlin("jvm") version "1.8.0-RC2"
    kotlin("plugin.serialization") version "1.8.0-RC2"
}

group = "org.hildan.aoc"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation("junit:junit:4.13.2")
}
