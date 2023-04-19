plugins {
    id("java")
}

group = "com.github.dominik48n.party"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.1")

    implementation(project(":api"))
}
