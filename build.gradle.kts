plugins {
    kotlin("jvm") version "1.6.0"
    kotlin("plugin.serialization") version "1.6.0"
    id("jacoco")
    id("org.openjfx.javafxplugin") version "0.0.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
}
version = "1.0.0"

val kotlinxSerializationJsonVersion: String by rootProject
val tornadofxVersion: String by rootProject
val mockkVersion: String by rootProject
val testfxVersion: String by rootProject
val monocleVersion: String by rootProject
val jacocoVersion: String by rootProject

repositories {
    mavenCentral()
    maven(uri("https://oss.sonatype.org/content/repositories/snapshots"))
}

application {
    mainClass.set("automaton.constructor.AutomatonConstructorAppKt")
}

javafx {
    version = "17"
    modules("javafx.controls")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationJsonVersion")
    implementation("no.tornado:tornadofx:$tornadofxVersion")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.testfx:testfx-core:$testfxVersion")
    testImplementation("org.testfx:testfx-junit5:$testfxVersion")
    testImplementation("org.testfx:openjfx-monocle:$monocleVersion")
}

jacoco {
    toolVersion = jacocoVersion
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    test {
        finalizedBy("jacocoTestReport")
        useJUnitPlatform()
        if (project.hasProperty("headless") && project.property("headless").toString().toBoolean()) {
            systemProperty("testfx.robot", "glass")
            systemProperty("testfx.headless", "true")
            systemProperty("prism.order", "sw")
            systemProperty("prism.text", "t2k")
        }
    }
    jacocoTestReport {
        dependsOn("test")
        reports {
            xml.required.set(false)
            html.required.set(true)
            csv.required.set(true)
        }
        classDirectories.setFrom(
            files(classDirectories.files.map {
                fileTree(it) {
                    include("**/model/**")
                    exclude("**/**/*serializer*.*") // exclude generated serializers
                }
            })
        )
    }
}
