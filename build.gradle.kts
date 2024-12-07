val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.0.0"
    id("io.ktor.plugin") version "3.0.2"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
}

group = "io.sn"
version = "0.1.0"

application {
    mainClass.set("io.sn.quaternium.ApplicationKt")

    // applicationDefaultJvmArgs = listOf("-Dio.ktor.development=false")
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    implementation("io.ktor:ktor-server-html-builder")
    implementation("io.ktor:ktor-server-sessions")
}
