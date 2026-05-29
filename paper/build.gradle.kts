plugins {
    java
    id("com.gradleup.shadow") version "9.4.1"
}

description = "CoreBau — Paper (Baul + Board + Selector + Npc)"

dependencies {
    val paperApi   = providers.gradleProperty("paperApiVersion").get()
    val hikari     = providers.gradleProperty("hikariVersion").get()
    val mysql      = providers.gradleProperty("mysqlConnectorVersion").get()
    val npcLib     = providers.gradleProperty("npcLibVersion").get()
    val caffeine   = providers.gradleProperty("caffeineVersion").get()
    val bstats     = providers.gradleProperty("bstatsVersion").get()
    val packets    = providers.gradleProperty("packeteventsVersion").get()

    compileOnly("io.papermc.paper:paper-api:$paperApi")

    implementation(project(":common"))

    // ---- Plataforma / plugins externos (nunca shadear) ----
    compileOnly("com.github.retrooper:packetevents-spigot:$packets")       // Npc
    compileOnly("net.dmulloy2:ProtocolLib:5.4.0")                          // Baul
    compileOnly("me.clip:placeholderapi:2.12.2")                           // Baul, Board, Selector
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")                      // Baul
    compileOnly("me.filoghost.holographicdisplays:holographicdisplays-api:3.0.0")
    compileOnly("com.github.decentsoftware-eu:decentholograms:2.9.9")
    compileOnly("com.mojang:authlib:3.13.56")                             // Selector
    compileOnly("net.luckperms:api:5.4")                                  // Npc
    compileOnly("org.projectlombok:lombok:1.18.44")
    annotationProcessor("org.projectlombok:lombok:1.18.44")

    // ---- Librerías que SÍ se shadean (relocadas en shadowJar) ----
    // Baul:
    implementation("com.googlecode.json-simple:json-simple:1.1")
    implementation("com.zaxxer:HikariCP:$hikari")
    implementation("io.github.bananapuncher714:nbteditor:8.0.0")
    implementation("io.github.almighty-satan:XSeries:13.6.0+26.1")
    implementation("org.jsoup:jsoup:1.22.1")
    // Npc:
    implementation("io.github.juliarn:npc-lib-bukkit:$npcLib") {
        exclude(group = "com.github.retrooper")
    }
    implementation("com.github.ben-manes.caffeine:caffeine:$caffeine")
    implementation("com.mysql:mysql-connector-j:$mysql")
    implementation("org.bstats:bstats-bukkit:$bstats")
    // Board:
    implementation("fr.mrmicky:fastboard:2.1.5")
    // Selector:
    implementation("com.github.MrMicky-FR:FastInv:3.1.1")

    implementation("org.jetbrains:annotations:26.1.0")
}

tasks {
    processResources {
        filteringCharset = "UTF-8"
        filesMatching("**/*.yml") {
            expand("version" to project.version)
        }
    }

    shadowJar {
        archiveBaseName.set("CoreBau")
        archiveClassifier.set("")
        // PacketEvents trae Multi-Release: true; lo desactivamos para que no rompa
        // el cierre del classloader (igual que el build original de Npc).
        manifest {
            attributes["Multi-Release"] = "false"
        }

        // Adventure/MiniMessage los provee Paper → NO se shadea (Baul deja de relocar kyori).

        // Relocations por módulo (se completan al portar cada uno en Etapa 4):
        relocate("com.zaxxer.hikari", "cl.xgamers.corebau.libs.hikari")
        relocate("io.github.bananapuncher714.nbteditor", "me.davidml16.baul.libs.nbteditor")
        relocate("com.cryptomorin.xseries", "me.davidml16.baul.libs.xseries")
        relocate("org.jsoup", "me.davidml16.baul.libs.jsoup")
        relocate("org.json.simple", "me.davidml16.baul.libs.jsonsimple")
        relocate("com.github.juliarn.npclib", "dev.blancocl.libs.npclib")
        relocate("com.github.benmanes.caffeine", "dev.blancocl.libs.caffeine")
        relocate("org.bstats", "dev.blancocl.libs.bstats")
        relocate("fr.mrmicky.fastboard", "cl.xgamers.board.fastboard")
        relocate("fr.mrmicky.fastinv", "cl.xgamers.selector.fastinv")
    }

    build {
        dependsOn(shadowJar)
    }
}
