plugins {
    id("java")
}

group = "com.github.dominik48n.party"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

dependencies {
    compileOnly("net.md-5:bungeecord-api:1.19-R0.1-SNAPSHOT")

    implementation(project(":common"))
}

tasks.processResources {
    filesMatching("bungee.yml") {
        expand(mapOf("version" to rootProject.version))
    }
}
