plugins {
    id("java")
    id("com.gradleup.shadow") version "9.3.0"
    application
    jacoco
}

group = "io.github.mcalgovisualizations"
version = "unspecified"

application {
    mainClass.set("io.github.mcalgovisualizations.Main")
}

repositories {
    mavenCentral()
}

sourceSets {
    main {
        java.srcDirs("src/main/java")
        resources.srcDir("resources")
    }
}

dependencies {
    // Server Dependencies
    implementation("net.minestom:minestom:2026.01.08-1.21.11")
    implementation("ch.qos.logback:logback-classic:1.5.13")

    // Project Dependency
    implementation(project(":lib"))

    // Testing Dependencies
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    manifest {
        attributes["Main-Class"] = "io.github.mcalgovisualizations.Main"
    }
    archiveBaseName.set("minecraft-server")
    archiveClassifier.set("all")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        html.required.set(true)
        xml.required.set(true)
        csv.required.set(false)
    }
}
