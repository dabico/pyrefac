group = rootProject.group
version = rootProject.version

val slf4jVersion = "2.0.13"
val javalinVersion = "6.1.4"
val jgitVersion = "6.9.0.202403050737-r"

dependencies {
    implementation(project(":pyrefac-core"))
    implementation("org.slf4j:slf4j-api:${slf4jVersion}")
    implementation("org.slf4j:slf4j-simple:${slf4jVersion}")
    implementation("io.javalin:javalin:${javalinVersion}")
    implementation("org.eclipse.jgit:org.eclipse.jgit:${jgitVersion}")
}

tasks {
    buildSearchableOptions {
        // https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin-faq.html
        enabled = false
    }

    runIde {
        val port: String? by project
        args = listOfNotNull("pyrefac-server", port)
        jvmArgs = listOf(
            // https://oracle.com/technical-resources/articles/javase/headless.html
            "-Djava.awt.headless=true",
            // https://plugins.jetbrains.com/docs/intellij/enabling-internal.html
            "-Didea.is.internal=false",
            "-Xmx8G",
        )
        maxHeapSize = "8g"
    }
    register("serve") {
        dependsOn(runIde)
    }

    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("242.*")
    }
}
