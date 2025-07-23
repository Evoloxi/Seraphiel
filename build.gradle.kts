import java.util.*

plugins {
    idea
    java
    alias(libs.plugins.weave)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.shadow)
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.2"
    id("net.kyori.blossom") version "2.1.0"
}

val group: String by project
val mcVersion: String by project
val version: String by project
val modId_: String by project

sourceSets {
    main {
        blossom {
            kotlinSources {
                val input = file("src/main/resources/token.secret").readText()
                property("token", input.trim())
            }
        }
    }
}

weave {
    configure {
        val modJson = file("src/main/resources/weave.mod.json").readText()
        val json = groovy.json.JsonSlurper().parseText(modJson) as Map<*, *>
        name = this.modId.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        modId = modId_
        entryPoints  = (json["entrypoints"] as List<*>).map { it.toString() }
        mixinConfigs = (json["mixinConfigs"] as List<*>).map { it.toString() }
        hooks        = (json["hooks"] as List<*>).map { it.toString() }
        mcpMappings()
    }
    version(mcVersion)
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.spongepowered.org/maven/")
    maven("https://gitlab.com/api/v4/projects/64882766/packages/maven")
    //maven("https://gitlab.com/api/v4/projects/64882633/packages/maven")
    //maven("https://gitlab.com/api/v4/projects/57325214/packages/maven")
}

dependencies {
    compileOnly(files("/home/evoloxi/Downloads/Weave-Loader/loader/build/libs/weave-loader-1.0.0-b.3-all.jar"))
    compileOnly(libs.mixin)
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.serialization) {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0") {
        exclude(group = "org.jetbrains.kotlin")
    }
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2") {
        exclude(group = "org.jetbrains.kotlin")
    }
}

tasks {
    shadowJar {
        archiveClassifier.set("")

        relocate("io.ktor", "me.evo.seraphiel.shaded.ktor")
        relocate("kotlinx.serialization", "me.evo.seraphiel.shaded.serialization")
        relocate("kotlinx.coroutines", "me.evo.seraphiel.shaded.coroutines")
        relocate("kotlin", "me.evo.seraphiel.shaded.kotlin")

        exclude("META-INF/versions/**")
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")
    }

    register("hotload") {
        dependsOn(shadowJar)
        dependsOn(build)
        doLast {
            copy {
                from(shadowJar)
                into(System.getProperty("user.home") + "/.weave/mods")
            }
        }
    }
}

kotlin {
    jvmToolchain(8)
}