import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val ktlint by configurations.creating

plugins {
    kotlin("jvm") version "2.2.0"
    id("com.gradleup.shadow") version "8.3.8"
}

group = "me.quantiom"
version = "1.2.8"
description = "AdvancedVanish"

repositories {
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.dmulloy2.net/nexus/repository/public/")
    maven("https://repo.essentialsx.net/releases/")
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://nexus.scarsz.me/content/groups/public/")
    maven("https://repo.codemc.org/repository/maven-public/")
    maven("https://repo.mikeprimm.com/")
    maven("https://repo.alessiodp.com/releases/")
    maven("https://repo.maven.apache.org/maven2/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.rosewooddev.io/repository/public/")
    maven("https://repo.md-5.net/content/groups/public/")
}

val versions =
    mapOf(
        "paperApi" to "1.21.3-R0.1-SNAPSHOT",
        "acfPaper" to "0.5.1-SNAPSHOT",
        "lombok" to "1.18.38",
        "libby" to "1.3.1",
        "kotlinStdlib" to "2.2.0",
        "protocolLib" to "5.3.0",
        "placeholderApi" to "2.11.6",
        "dynmap" to "3.7-beta-6",
        "squaremap" to "1.3.7",
        "playerParticles" to "8.9",
        "jedis" to "6.0.0",
        "exposed" to "0.61.0",
        "discordSRV" to "1.29.0",
        "essentials" to "2.21.1",
        "libsdisguises" to "10.0.44",
        "groupmanager" to "3.2",
        "luckperms" to "5.5",
        "ktlint" to "1.7.0",
    )

dependencies {
    api("co.aikar:acf-paper:${versions["acfPaper"]}")
    api("org.projectlombok:lombok:${versions["lombok"]}")
    api("net.byteflux:libby-bukkit:${versions["libby"]}")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:${versions["kotlinStdlib"]}")
    compileOnly("io.papermc.paper:paper-api:${versions["paperApi"]}")
    compileOnly("com.comphenix.protocol:ProtocolLib:${versions["protocolLib"]}")
    compileOnly("me.clip:placeholderapi:${versions["placeholderApi"]}")
    compileOnly("us.dynmap:DynmapCoreAPI:${versions["dynmap"]}")
    compileOnly("xyz.jpenilla:squaremap-api:${versions["squaremap"]}")
    compileOnly("dev.esophose:playerparticles:${versions["playerParticles"]}")
    compileOnly("redis.clients:jedis:${versions["jedis"]}")
    compileOnly("org.jetbrains.exposed:exposed-core:${versions["exposed"]}")
    compileOnly("org.jetbrains.exposed:exposed-dao:${versions["exposed"]}")
    compileOnly("org.jetbrains.exposed:exposed-jdbc:${versions["exposed"]}")
    compileOnly("com.discordsrv:discordsrv:${versions["discordSRV"]}")
    compileOnly("net.essentialsx:EssentialsX:${versions["essentials"]}")
    compileOnly("com.github.ElgarL:groupmanager:${versions["groupmanager"]}")
    compileOnly("net.luckperms:api:${versions["luckperms"]}")
    compileOnly("LibsDisguises:LibsDisguises:${versions["libsdisguises"]}") {
        exclude(group = "org.spigotmc", module = "spigot")
    }
    ktlint("com.pinterest.ktlint:ktlint-cli:${versions["ktlint"]}") {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        }
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    withSourcesJar()
}
sourceSets {
    main {
        java {
            srcDirs("src/main/kotlin") // Ensure Gradle is compiling files from this directory
        }
    }
}
tasks {
    named<ProcessResources>("processResources") {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    withType<Javadoc>().configureEach {
        options.encoding = "UTF-8"
    }

    register<JavaExec>("ktlintCheck") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Check Kotlin code style"
        classpath = configurations.getByName("ktlint")
        mainClass.set("com.pinterest.ktlint.Main")
        args("**/src/**/*.kt", "**.kts", "!**/build/**")
    }

    register<JavaExec>("ktlintFormat") {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
        description = "Format Kotlin code"
        classpath = configurations.getByName("ktlint")
        mainClass.set("com.pinterest.ktlint.Main")
        args("-F", "**/src/**/*.kt", "**.kts", "!**/build/**")
    }

    withType<ShadowJar>().configureEach {
        minimize()
        relocate("net.byteflux.libby", "me.quantiom.libs.libby")
        relocate("co.aikar.commands", "me.quantiom.libs.acf")
        relocate("redis.clients.jedis", "me.quantiom.libs.jedis")
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
        archiveClassifier.set("")
    }

    named("build") {
        dependsOn("shadowJar")
    }
}
