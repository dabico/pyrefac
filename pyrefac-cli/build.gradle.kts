group = rootProject.group
version = rootProject.version

dependencies {
    implementation(project(":pyrefac-core"))
}

tasks {
    compileJava {
        val projectArgs = listOfNotNull("-Aproject=${project.group}/${project.name}")
        options.compilerArgs.plusAssign(projectArgs)
    }

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
            "-Xmx8G",
        )
        maxHeapSize = "8g"
    }
    register("runPyRefac") {
        dependsOn(runIde)
    }

    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("242.*")
    }
}
