import xyz.jpenilla.resourcefactory.bukkit.Permission

plugins {
    alias(libs.plugins.zapper)
    alias(libs.plugins.bukkit.factory)      // Bukkit resource factory plugin for generating plugin.yml at build time
    alias(libs.plugins.run.paper)           // The run-task plugin for running a test server and testing the plugin
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.marcely.de/repository/maven-public/")
}

dependencies {
    zap("org.jetbrains.kotlin:kotlin-stdlib:2.2.20")

    compileOnly(libs.spigot)
    compileOnly(libs.mbedwars)

    implementation(libs.bstats)

    zap(libs.boostedyaml)
    zap(libs.caffeine)
    zap(libs.hikaricp)
    zap(libs.exposed.core)
    zap(libs.exposed.jdbc)
    zap(libs.mariadb.java.client)
    zap(libs.h2.database)
    zap(libs.triumph.gui)
}

zapper {
    libsFolder = "../MBedwars/add-ons/MB-MatchVault/libs"
    relocationPrefix = "me.hosairis.matchvault"

    repositories { includeProjectRepositories() }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(17) // Use Java 17 toolchain
}

// Output Java 8-compatible bytecode for Java classes
tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
}

// Output Java 8-compatible bytecode (For Kotlin)
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
    }
}

tasks {
    runServer {
        minecraftVersion("1.18.2") // Configure the Minecraft server version.
        downloadPlugins {
            // Download ViaVersion and ViaBackwards plugins.
            modrinth("viaversion", "5.2.2-SNAPSHOT+686")
            modrinth("viabackwards", "5.2.2-SNAPSHOT+391")
        }
    }

    shadowJar {
        relocate("org.bstats", "me.hosairis.matchvault.libs.bstats")
        relocate("dev.dejvokep.boostedyaml", "me.hosairis.matchvault.libs.boostedyaml")

        archiveFileName.set("${rootProject.name}-${project.version}.jar")
        minimize()
    }
}

bukkitPluginYaml {
    name = "MB-MatchVault"
    main = "me.hosairis.matchvault.MatchVault"
    authors.add("Hosairis")
    apiVersion = "1.13"
    depend.add("MBedwars")

    commands {
        create("matchvault") {
            description = "Main command for MatchVault"
            usage = "/matchvault <subcommand>"
            aliases.addAll(listOf("mvault", "matchv", "mva", "mava"))
        }

        create("matchhistory") {
            description = "Command for viewing bedwars match history"
            usage = "/matchhistory [player]"
            permission = "mva.commands.history"
            permissionMessage = "%prefix &7You lack the required permission: &cmva.commands.history"
        }
    }

    permissions {
        create("mva.commands.reload") {
            default = Permission.Default.OP
        }

        create("mva.commands.history") {
            default = Permission.Default.OP
        }

        create("mva.commands.history.others") {
            default = Permission.Default.OP
            children.set(mapOf("mva.commands.history" to true))
        }
    }
}
