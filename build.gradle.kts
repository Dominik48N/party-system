plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("checkstyle")
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
    apply(plugin = "checkstyle")

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
    }

    tasks.named<Jar>("shadowJar") {
        archiveBaseName.set("${rootProject.name}-${project.name}")
        archiveClassifier.set("")
    }

    configure<CheckstyleExtension> {
        toolVersion = "8.45"
        configFile = rootProject.file("config/checkstyle/google_checks.xml")
        isIgnoreFailures = false
    }

    val checkTask = tasks.getByName("check")
    checkTask.dependsOn("checkstyleMain", "checkstyleTest")
}
