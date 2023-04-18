plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

repositories {
    mavenCentral()
}

allprojects {
    group = "com.github.dominik48n.party"
    version = "1.0.0"
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "com.github.johnrengelman.shadow")

    repositories {
        mavenCentral()
    }

    tasks.named<Jar>("shadowJar") {
        archiveBaseName.set("${rootProject.name}-${project.name}")
        archiveClassifier.set("")
    }
}
