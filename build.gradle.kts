// ---- Configuración común a todos los subproyectos del monorepo CoreBau ----

plugins {
    java
}

allprojects {
    group = "cl.xgamers.corebau"
    version = providers.gradleProperty("coreBauVersion").get()

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")   // Paper + Velocity
        maven("https://jitpack.io")
        maven("https://repo.codemc.io/repository/maven-public/")     // NBTEditor, FastInv, etc.
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://repo.dmulloy2.net/nexus/repository/public/")  // ProtocolLib
        maven("https://repository.derklaro.dev/releases/")           // npc-lib
        maven("https://oss.sonatype.org/content/repositories/snapshots/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://libraries.minecraft.net/")                    // authlib
        mavenLocal()
    }
}

subprojects {
    apply(plugin = "java")

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
}
