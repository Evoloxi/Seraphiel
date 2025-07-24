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

    val lunar by registering(JavaExec::class) {
        val lunarHome = File(System.getProperty("user.home"), ".lunarclient")

        val jreDir = File(lunarHome, "jre")
        val jreVersionDirs = jreDir.listFiles { file -> file.isDirectory }

        if (jreVersionDirs == null || jreVersionDirs.isEmpty()) {
            throw GradleException("No JRE directories found in ${jreDir.absolutePath}")
        }

        val jreHome = jreVersionDirs.first().listFiles {
            file -> file.isDirectory
        }?.first() ?: throw GradleException("No JRE home directory found in ${jreVersionDirs.first().absolutePath}")

        val javaExecutable = File(jreHome, "bin/java")
        if (!javaExecutable.exists()) {
            throw GradleException("Java executable not found at ${javaExecutable.absolutePath}")
        }

        executable(javaExecutable)
        mainClass.set("com.moonsworth.lunar.genesis.Genesis")

        val gameCacheDir = File(lunarHome, "offline/multiver")
        if (!gameCacheDir.exists()) {
            throw GradleException("Lunar client game cache directory not found at ${gameCacheDir.absolutePath}")
        }
        workingDir = gameCacheDir

        val loader = File(System.getProperty("user.home"), ".weave/weave-loader-1.0.0-b.3-all.jar").absolutePath

        doFirst {
            try {
                println("Attempting to kill any running Java processes from Lunar Client...")
                Runtime.getRuntime().exec(arrayOf("killall", "-9", javaExecutable.absolutePath)).waitFor()
                Thread.sleep(100)
            } catch (e: Throwable) {
                println("Failed to kill previous processes: ${e.message}")
            }
        }

        jvmArgs = listOf(
            "--add-modules", "jdk.naming.dns",
            "--add-exports", "jdk.naming.dns/com.sun.jndi.dns=java.naming",
            "-Djna.boot.library.path=natives",
            "-Dlog4j2.formatMsgNoLookups=true",
            "--add-opens", "java.base/java.io=ALL-UNNAMED",
            "-XX:+UseStringDeduplication",
            "-Dichor.filteredGenesisSentries=.*lcqt.*|.*Some of your mods are incompatible with the game or each other.*",
            "-Dlunar.webosr.url=file:index.html",
            "-Xmx4124m",
            "-Dichor.fabric.localModPath=${System.getProperty("user.home")}/.lunarclient/profiles/lunar/1.8/mods",
            "-Djava.library.path=natives",
            "-XX:-CreateCoredumpOnCrash",
            "-XX:-CreateMinidumpOnCrash",
            "-javaagent:$loader"
        )

        val jarFiles = workingDir.listFiles { _, name -> name?.endsWith(".jar") == true }?.filter {
            listOf(
                "common-0.1.0-SNAPSHOT-all.jar",
                "genesis-0.1.0-SNAPSHOT-all.jar",
                "legacy-0.1.0-SNAPSHOT-all.jar",
                "lunar-lang.jar",
                "lunar-emote.jar",
                "lunar.jar",
                "optifine-0.1.0-SNAPSHOT-all.jar"
            ).contains(it.name)
        }

        if (jarFiles == null || jarFiles.isEmpty()) {
            throw GradleException("Required JAR files not found in ${workingDir.absolutePath}")
        }
        classpath = files(*jarFiles.toTypedArray())

        args = listOf(
            "--version"           , "1.8.9",
            "--launcherVersion"   , "3.4.6-ow",
            "--accessToken"       , "${ environment["ACCESS_TOKEN"] ?: "0" }",
            "--username"          , "${ environment["USERNAME"] ?: "Player" }",
            "--uuid"              , "${ environment["UUID"] ?: UUID.randomUUID().toString().replace("-", "") }",
            "--userProperties"    , "{}",
            "--gameDir"           , File(System.getProperty("user.home"), ".minecraft").absolutePath,
            "--texturesDir"       , File(lunarHome, "textures").absolutePath,
            "--uiDir"             , File(lunarHome, "ui").absolutePath,
            "--webosrDir"         , "${workingDir}/natives",
            "--workingDirectory"  , ".",
            "--classpathDir"      , ".",
            "--width"             , "854",
            "--height"            , "480",
            "--assetIndex"        , "1.8",
            "--launchId"          , "manual-launch-${UUID.randomUUID()}",
            "--installationId"    , "${ environment["ACCESS_TOKEN"] ?: UUID.randomUUID() }",
            "--ipcPort"           , "28190",
            "--ichorClassPath"    , jarFiles.joinToString(",") { it.name },
            "--ichorExternalFiles", "OptiFine_v1_8.jar"
        )
    }

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