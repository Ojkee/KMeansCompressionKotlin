plugins {
    kotlin("jvm") version "2.1.20"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.java.dev.jna:jna:5.14.0")
}

application {
    mainClass.set("MainKt")
    applicationDefaultJvmArgs =
        listOf(
            "-Djna.library.path=/usr/lib",
        )
}

kotlin {
    jvmToolchain(21)
}
