import io.papermc.hangarpublishplugin.model.Platforms
import kotlin.system.exitProcess
import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription
import xyz.jpenilla.runpaper.task.RunServer

plugins {
    id("java")
    alias(libs.plugins.shadowJar)
    alias(libs.plugins.publishdata)
    alias(libs.plugins.paper.run)
    alias(libs.plugins.paper.yml)
    alias(libs.plugins.hangar)
    alias(libs.plugins.modrinth)
    alias(libs.plugins.spotless)
    id("olf.build-logic")
    `maven-publish`
}

if (!File("$rootDir/.git").exists()) {
    logger.lifecycle("""
    **************************************************************************************
    You need to fork and clone this repository! Don't download a .zip file.
    If you need assistance, consult the GitHub docs: https://docs.github.com/get-started/quickstart/fork-a-repo
    **************************************************************************************
    """.trimIndent()
    ).also { exitProcess(1) }
}

allprojects {
    group = "net.onelitefeather.bettergopaint"
    version = "1.1.0"
}
group = "net.onelitefeather.bettergopaint"

val supportedMinecraftVersions = listOf(
        "1.21"
)

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    // Paper / Spigot
    compileOnly(libs.paper)
    // Fawe / WorldEdit
    implementation(platform(libs.fawe.bom))
    compileOnly(libs.fawe.bukkit)
    // Utils
    implementation(libs.serverlib)
    implementation(libs.paperlib)
    implementation(libs.semver)
    // Stats
    implementation(libs.bstats)
    // Commands
    implementation(libs.cloud.command.annotations)
    implementation(libs.cloud.command.extras)
    implementation(libs.cloud.command.paper)
    annotationProcessor(libs.cloud.command.annotations)
}

publishData {
    useEldoNexusRepos(false)
    publishTask("shadowJar")
}


paper {
    name = "BetterGoPaint"
    main = "net.onelitefeather.bettergopaint.BetterGoPaint"
    authors = listOf("Arcaniax", "TheMeinerLP")
    apiVersion = "1.20"

    serverDependencies {
        register("FastAsyncWorldEdit") {
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            required = true
        }
    }

    website = "https://github.com/OneLiteFeatherNET/BetterGoPaint"
    provides = listOf("goPaint")

    permissions {
        register("bettergopaint.command.admin.reload") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("bettergopaint.notify.admin.update") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("bettergopaint.notify.disable.donation") {
            default = BukkitPluginDescription.Permission.Default.FALSE
        }
        register("bettergopaint.use") {
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("bettergopaint.admin") {
            default = BukkitPluginDescription.Permission.Default.FALSE
        }
        register("bettergopaint.world.bypass") {
            default = BukkitPluginDescription.Permission.Default.FALSE
        }
    }
}


spotless {
    java {
        licenseHeaderFile(rootProject.file("HEADER.txt"))
        target("**/*.java")
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks {
    jar {
        archiveClassifier.set("unshaded")
    }
    compileJava {
        options.release.set(21)
        options.encoding = "UTF-8"
    }
    shadowJar {
        archiveClassifier.set("")
        dependencies {
            relocate("com.cryptomorin.xseries", "${rootProject.group}.xseries")
            relocate("org.incendo.serverlib", "${rootProject.group}.serverlib")
            relocate("org.bstats", "${rootProject.group}.metrics")
            relocate("io.papermc.lib", "${rootProject.group}.paperlib")
        }
    }
    build {
        dependsOn(shadowJar)
    }
    supportedMinecraftVersions.forEach { serverVersion ->
        register<RunServer>("run-$serverVersion") {
            minecraftVersion(serverVersion)
            jvmArgs("-DPaper.IgnoreJavaVersion=true", "-Dcom.mojang.eula.agree=true")
            group = "run paper"
            runDirectory.set(file("run-$serverVersion"))
            pluginJars(rootProject.tasks.shadowJar.map { it.archiveFile }.get())
        }
    }
}

val branch = rootProject.branchName()
val baseVersion = publishData.getVersion(false)
val isRelease = !baseVersion.contains('-')
val isMainBranch = branch == "master"
if (!isRelease || isMainBranch) { // Only publish releases from the main branch
    val suffixedVersion = if (isRelease) baseVersion else baseVersion + "+" + System.getenv("GITHUB_RUN_NUMBER")
    val changelogContent = if (isRelease) {
        "See [GitHub](https://github.com/OneLiteFeatherNET/BetterGoPaint) for release notes."
    } else {
        val commitHash = rootProject.latestCommitHash()
        "[$commitHash](https://github.com/OneLiteFeatherNET/BetterGoPaint/commit/$commitHash) ${rootProject.latestCommitMessage()}"
    }
    hangarPublish {
        publications.register("BetterGoPaint") {
            version.set(suffixedVersion)
            channel.set(if (isRelease) "Release" else "Snapshot")
            changelog.set(changelogContent)
            apiKey.set(System.getenv("HANGAR_SECRET"))
            id.set("BetterGoPaint")
            platforms {
                register(Platforms.PAPER) {
                    jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                    platformVersions.set(supportedMinecraftVersions)
                }
            }
        }
    }

    modrinth {
        token.set(System.getenv("MODRINTH_TOKEN"))
        projectId.set("qf7sNg9A")
        versionType.set(if (isRelease) "release" else "beta")
        versionNumber.set(suffixedVersion)
        versionName.set(suffixedVersion)
        changelog.set(changelogContent)
        uploadFile.set(tasks.shadowJar.flatMap { it.archiveFile })
        gameVersions.addAll(supportedMinecraftVersions)
        loaders.add("paper")
        loaders.add("bukkit")
        loaders.add("folia")
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        // Configure our maven publication
        publishData.configurePublication(this)
    }

    repositories {
        // We add EldoNexus as our repository. The used url is defined by the publish data.
        maven {
            authentication {
                credentials(PasswordCredentials::class) {
                    // Those credentials need to be set under "Settings -> Secrets -> Actions" in your repository
                    username = System.getenv("ELDO_USERNAME")
                    password = System.getenv("ELDO_PASSWORD")
                }
            }

            name = "EldoNexus"
            setUrl(publishData.getRepository())
        }
    }
}
