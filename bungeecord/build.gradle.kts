plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "com.github.dominik48n.party"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    compileOnly("net.md-5:bungeecord-api:1.19-R0.1-SNAPSHOT")

    implementation("net.kyori:adventure-api:4.13.1")
    implementation("net.kyori:adventure-text-minimessage:4.13.1")
    implementation("net.kyori:adventure-platform-bungeecord:4.3.0")
    implementation("org.jetbrains:annotations:24.0.1")
    implementation(project(":common"))
    implementation(project(":api"))
}

tasks.shadowJar {
    relocate("net.kyori", "${project.group}.libs.kyori")
    relocate("org.jetbrains", "${project.group}.libs.jetbrains")
    relocate("redis.clients", "${project.group}.libs.redis")
    relocate("org.apache.commons.pool2", "${project.group}.libs.commons.pool2")
    relocate("org.intellij.lang", "${project.group}.libs.intellij.lang")
    relocate("org.json", "${project.group}.libs.json")
    relocate("org.slf4j", "${project.group}.libs.slf4j")
    relocate("com.google.gson", "${project.group}.libs.gson")
}

tasks.processResources {
    filesMatching("bungee.yml") {
        expand(mapOf("version" to rootProject.version))
    }
}
