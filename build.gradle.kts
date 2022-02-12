plugins {
    kotlin("jvm") version "1.6.0"
    id("jacoco")
    id("org.openjfx.javafxplugin") version "0.0.10"
    application
}
version = "1.0-SNAPSHOT"

val tornadofxVersion: String by rootProject
val mockkVersion: String by rootProject
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
    implementation("no.tornado:tornadofx:$tornadofxVersion")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.mockk:mockk:$mockkVersion")
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
    }
    jacocoTestReport {
        dependsOn("test")
        reports {
            xml.required.set(false)
            html.required.set(true)
            csv.required.set(true)
        }
    }
}
