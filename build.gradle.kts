plugins {
    `maven-publish`
}

tasks.named("publishToMavenLocal") {
    dependsOn(":lib:publishToMavenLocal", ":prefab:publishToMavenLocal")
}
