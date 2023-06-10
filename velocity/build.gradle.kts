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
    implementation("redis.clients:jedis:4.3.2")
    implementation("com.google.guava:guava:31.1-jre")
}

tasks.shadowJar {
    relocate("redis.clients", "${project.group}.libs.redis")
    relocate("org.apache.commons.pool2", "${project.group}.libs.commons.pool2")
    relocate("org.json", "${project.group}.libs.json")
    relocate("com.fasterxml.jackson", "${project.group}.libs.jackson")
    relocate("com.google.common.collect", "${project.group}.libs.commons.google.collect")
    relocate("org.bson", "${project.group}.libs.bson")
    relocate("com.mongodb", "${project.group}.libs.mongodb")
    relocate("com.postgresql", "${project.group}.libs.postgresql")
    relocate("com.zaxxer.hikari", "${project.group}.libs.hikari")
}

blossom {
    replaceToken("@version@", rootProject.version, "src/main/java/com/github/dominik48n/party/velocity/PartyVelocityPlugin.java")
}
