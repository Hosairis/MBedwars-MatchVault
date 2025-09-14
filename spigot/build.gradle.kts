plugins {
    alias(libs.plugins.zapper)
    alias(libs.plugins.bukkit.factory)      // Bukkit resource factory plugin for generating plugin.yml at build time
    alias(libs.plugins.run.paper)           // The run-task plugin for running a test server and testing the plugin
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    zap("org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.2.20")

    compileOnly(libs.spigot)

    // implementation(libs.bstats)
}

zapper {
    libsFolder = "libs"
    relocationPrefix = "me.hosairis.matchvault"

    repositories {
        includeProjectRepositories()
    }
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

//    shadowJar {
//        relocate("org.bstats", "example.plugin.template.libs.bstats")
//    }
}

bukkitPluginYaml {
    name = "MB-MatchVault"
    main = "me.hosairis.matchvault.MatchVault"
    authors.add("Hosairis")
    apiVersion = "1.13"
}
