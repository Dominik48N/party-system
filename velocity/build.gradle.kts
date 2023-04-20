plugins {
    id("java")
    id("net.kyori.blossom") version "1.2.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "com.github.dominik48n.party"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    annotationProcessor("com.velocitypowered:velocity-api:3.1.1")

    implementation(project(":common"))
    implementation(project(":api"))
}

tasks.shadowJar {
    relocate("redis.clients", "${project.group}.libs.redis")
    relocate("org.apache.commons.pool2", "${project.group}.libs.commons.pool2")
    relocate("org.json", "${project.group}.libs.json")
    relocate("org.slf4j", "${project.group}.libs.slf4j")
    relocate("com.google.gson", "${project.group}.libs.gson")
}

blossom {
    replaceToken("@version@", rootProject.version, "src/main/java/com/github/dominik48n/party/velocity/PartyVelocityPlugin.java")
}
