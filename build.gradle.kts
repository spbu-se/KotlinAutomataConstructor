plugins {
    kotlin("jvm") version "1.4.32"
    application
}
version = "1.0-SNAPSHOT"

val tornadofxVersion: String by rootProject
val richtextfxVersion: String by rootProject

repositories {
    mavenCentral()
}

application {
    mainClassName = "automaton.constructor.AutomatonConstructorAppKt"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("no.tornado:tornadofx:$tornadofxVersion")
    implementation("org.fxmisc.richtext:richtextfx:$richtextfxVersion")
    testImplementation(kotlin("test-junit"))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}
