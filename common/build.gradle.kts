plugins {
    id("java")
}

group = "com.github.dominik48n.party"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.1")
    compileOnly("com.google.guava:guava:31.1-jre")
    compileOnly("net.kyori:adventure-api:4.13.1")

    implementation(project(":api"))
}
