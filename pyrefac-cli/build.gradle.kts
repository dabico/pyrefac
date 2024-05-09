group = rootProject.group
version = rootProject.version

val picocliVersion = "4.7.6"

dependencies {
    implementation(project(":pyrefac-core"))
    implementation("info.picocli:picocli:${picocliVersion}")
    annotationProcessor("info.picocli:picocli-codegen:${picocliVersion}")
}

tasks {
    compileJava {
        val projectArgs = listOfNotNull("-Aproject=${project.group}/${project.name}")
        options.compilerArgs.plusAssign(projectArgs)
    }

    buildSearchableOptions {
        // https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin-faq.html
        enabled = false
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
