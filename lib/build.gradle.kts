plugins {
    `java-library`
    `maven-publish`
    jacoco
}

repositories {
    mavenCentral()
}

dependencies {
    // JUnit 5
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Mockito Core (Enables inline mock maker for final classes on JDK 17+)
    testImplementation("org.mockito:mockito-core:5.14.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.0")

    // Minestom
    testImplementation("net.minestom:minestom:2026.01.08-1.21.11")

    api(libs.commons.math3)
    implementation(libs.guava)
    compileOnly("net.minestom:minestom:2026.01.08-1.21.11")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.test {
    useJUnitPlatform()
    jvmArgs(
        "-XX:+EnableDynamicAgentLoading",
        "-Dnet.bytebuddy.experimental=true"
    )
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
