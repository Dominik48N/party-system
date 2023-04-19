plugins {
    id("java")
    id("net.kyori.blossom") version "1.2.0"
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

blossom {
    replaceToken("@version@", rootProject.version, "src/main/java/com/github/dominik48n/party/velocity/PartyVelocityPlugin.java")
}
