group = rootProject.group
version = rootProject.version

val junitVersion = "5.10.2"

dependencies {
    testImplementation(platform("org.junit:junit-bom:${junitVersion}"))
    testImplementation("org.junit.jupiter:junit-jupiter:${junitVersion}")
}

tasks {
    test {
        useJUnitPlatform()
        jvmArgs = listOf("-Djdk.module.illegalAccess.silent=true")
    }
}
