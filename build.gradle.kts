group = "ch.usi.si.seart.pyrefac"
version = "1.0.0-SNAPSHOT"

plugins {
    id("java")
    id("org.jetbrains.intellij") version "1.17.3"
}

allprojects {
    apply {
        plugin("java")
        plugin("org.jetbrains.intellij")
    }

    repositories {
        mavenCentral()
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
    // https://plugins.jetbrains.com/docs/intellij/intellij-community-plugins-extension-point-list.html
    intellij {
        type.set("IC")
        version.set("2023.2.6")
        plugins.set(
            listOfNotNull(
                "Git4Idea",
                "PythonCore:232.10300.40",
            )
        )
    }
}
