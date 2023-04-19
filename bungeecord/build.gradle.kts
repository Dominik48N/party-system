plugins {
    id("java")
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

tasks.processResources {
    filesMatching("bungee.yml") {
        expand(mapOf("version" to rootProject.version))
    }
}
