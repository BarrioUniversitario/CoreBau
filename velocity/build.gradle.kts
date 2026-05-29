plugins {
    java
    id("com.gradleup.shadow") version "9.4.1"
}

description = "CoreBau — Velocity (proxy)"

dependencies {
    val velocityApi = providers.gradleProperty("velocityApiVersion").get()
    compileOnly("com.velocitypowered:velocity-api:$velocityApi")
    annotationProcessor("com.velocitypowered:velocity-api:$velocityApi")

    implementation(project(":common"))
}

tasks {
    // velocity-plugin.json lo genera el annotation processor desde @Plugin en Core.java.

    shadowJar {
        archiveBaseName.set("Core")
        archiveClassifier.set("")
    }

    build {
        dependsOn(shadowJar)
    }
}
