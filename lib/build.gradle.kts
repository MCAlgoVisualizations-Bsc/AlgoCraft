plugins {
    `java-library`
    `maven-publish`
    jacoco
}

repositories {
    mavenCentral()
}

dependencies {
    // JUnit 5 (Jupiter)
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Minestom available to lib tests
    testImplementation("net.minestom:minestom:2026.01.08-1.21.11")

    // Library deps
    api(libs.commons.math3)
    implementation(libs.guava)

    // Compile against Minestom without exporting it to consumers
    compileOnly("net.minestom:minestom:2026.01.08-1.21.11")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.test {
    useJUnitPlatform()
}

jacoco {
    toolVersion = "0.8.14"
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
