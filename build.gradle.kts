import com.inet.gradle.setup.abstracts.DesktopStarter.Location

import org.openjfx.gradle.JavaFXModule
import org.openjfx.gradle.JavaFXPlatform

plugins {
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.serialization") version "1.8.0"
    id("jacoco")
    id("org.openjfx.javafxplugin") version "0.0.10"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("de.inetsoftware.setupbuilder") version "7.2.16"
    id("edu.sc.seis.launch4j") version "2.5.4"
    application
}

val appMainClass = "automaton.constructor.AutomatonConstructorAppKt"
val exeFile = "automata_constructor.exe"
val appName = "Automata Constructor"
val addOpensPackages = listOf("java.base/java.lang")
val appJvmOptions = addOpensPackages.map { "--add-opens=$it=ALL-UNNAMED" }
val icoFile = "icon.ico"

val appVersion: String by rootProject
val kotlinxSerializationJsonVersion: String by rootProject
val tornadofxVersion: String by rootProject
val elkVersion: String by rootProject
val eclipseCoreVersion: String by rootProject
val betterParseVersion: String by rootProject
val mockkVersion: String by rootProject
val testfxVersion: String by rootProject
val monocleVersion: String by rootProject
val jacocoVersion: String by rootProject
val jvmTarget: String by rootProject

version = appVersion

repositories {
    mavenCentral()
    maven(uri("https://oss.sonatype.org/content/repositories/snapshots"))
}

application {
    mainClass.set(appMainClass)
    applicationDefaultJvmArgs += appJvmOptions
}

javafx {
    version = "17"
    modules("javafx.controls")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationJsonVersion")
    implementation("no.tornado:tornadofx:$tornadofxVersion")
    implementation("org.eclipse.elk:org.eclipse.elk.core:$elkVersion")
    implementation("org.eclipse.elk:org.eclipse.elk.graph:$elkVersion")
    implementation("org.eclipse.elk:org.eclipse.elk.alg.common:$elkVersion")
    implementation("org.eclipse.elk:org.eclipse.elk.alg.layered:$elkVersion")
    implementation("org.eclipse.elk:org.eclipse.elk.graph.text:$elkVersion")
    implementation("org.eclipse.elk:org.eclipse.elk.alg.graphviz.layouter:$elkVersion")
    implementation("org.eclipse.core:org.eclipse.core.runtime:$eclipseCoreVersion")
    implementation("com.github.h0tk3y.betterParse:better-parse:$betterParseVersion")

    for (module in JavaFXModule.getJavaFXModules(javafx.modules))
        for (platform in JavaFXPlatform.values())
            implementation("org.openjfx:${module.artifactName}:17:${platform.classifier}")

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
    compileJava {
        targetCompatibility = jvmTarget
    }
    compileKotlin {
        kotlinOptions.jvmTarget = jvmTarget
    }
    compileTestJava {
        targetCompatibility = jvmTarget
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = jvmTarget
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

    setupBuilder {
        vendor = "spbu-se"
        application = appName
        appIdentifier = "automaton.constructor"
        version = "1.1.1.0"
        licenseFile("LICENSE")
        icons = icoFile
        bundleJre = System.getenv("JAVA_HOME")

        desktopStarter {
            location = Location.DesktopDir
            displayName = appName
            executable = exeFile
            documentType {
                fileExtension = listOf("*.atmtn")
                name = "$appName file"
                role = "Editor"
            }
        }

        runAfter {
            executable = exeFile
        }
    }

    msi {
        dependsOn(createExe)

        from ("build/launch4j") {
            include("**")
        }

        setBannerBmp("banner.bmp")
        setDialogBmp("dialog.bmp")
        setLanguages(listOf("en-US", "ru-RU"))
        setWxsTemplate("template.wxs")
    }

    createExe {
        mainClassName = appMainClass
        outfile = exeFile
        icon = "$projectDir\\$icoFile"
        bundledJrePath = "jre"
        jvmOptions.addAll(appJvmOptions)
    }

    shadowJar {
        manifest {
            attributes["Add-Opens"] = addOpensPackages.joinToString(" ")
        }
    }
}
