plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.3"
}

group = "ch.usi.si.seart.pyrefac"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    type.set("IC")
    version.set("2023.2.6")
    plugins.set(emptyList())
}

tasks {
    runIde {
        val repository: String? by project
        val filePath: String? by project
        val refactoring: String? by project
        val parameters: String? by project
        args = listOfNotNull("pyrefac", repository, filePath, refactoring, parameters)
        jvmArgs = listOf(
            // https://oracle.com/technical-resources/articles/javase/headless.html
            "-Djava.awt.headless=true",
            // https://plugins.jetbrains.com/docs/intellij/enabling-internal.html
            "-Didea.is.internal=false",
        )
    }

    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("242.*")
    }
}
