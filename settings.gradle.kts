rootProject.name = "Seraphiel"

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
        maven("https://gitlab.com/api/v4/projects/64882766/packages/maven") // Weave Internals
        maven("https://gitlab.com/api/v4/projects/57325214/packages/maven") // Weave Gradle
    }
}