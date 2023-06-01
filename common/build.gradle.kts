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
    compileOnly("net.kyori:adventure-text-minimessage:4.13.1")

    implementation(project(":api"))
    implementation("redis.clients:jedis:4.3.2")

    testImplementation("net.kyori:adventure-api:4.13.1")
    testImplementation("net.kyori:adventure-text-minimessage:4.13.1")
    testImplementation("com.google.guava:guava:31.1-jre")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.mockito:mockito-junit-jupiter:5.3.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
