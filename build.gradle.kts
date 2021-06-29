import nl.javadude.gradle.plugins.license.LicensePlugin
import org.checkerframework.gradle.plugin.CheckerFrameworkPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformJvmPlugin
import net.kyori.indra.IndraPlugin
import net.kyori.indra.IndraPublishingPlugin
import java.util.*

plugins {
    java
    `java-library`
    checkstyle
    jacoco
    idea
    kotlin("jvm") version "1.5.10"
    id("com.github.hierynomus.license") version "0.16.1"
    id("org.checkerframework") version "0.5.22"
    id("net.kyori.indra") version "2.0.5"
    id("net.kyori.indra.publishing") apply false version "2.0.5"
}

allprojects {
    group = "net.kyori.moonshine"
    version = "2.0.0-SNAPSHOT"
}

subprojects {
    apply {
        plugin<JavaPlugin>()
        plugin<JavaLibraryPlugin>()
        plugin<MavenPublishPlugin>()
        plugin<CheckstylePlugin>()
        plugin<JacocoPlugin>()
        plugin<IdeaPlugin>()
        plugin<KotlinPlatformJvmPlugin>()
        plugin<LicensePlugin>()
        plugin<CheckerFrameworkPlugin>()
        apply<IndraPlugin>()
        apply<IndraPublishingPlugin>()
    }

    dependencies {
        api("com.google.guava:guava:30.1-jre")
        api("io.leangen.geantyref:geantyref:1.3.4")

        testImplementation("org.junit.jupiter:junit-jupiter:5.+")
        testImplementation("org.assertj:assertj-core:3.+")
        testImplementation(kotlin("stdlib-jdk8"))
        testImplementation(kotlin("reflect"))
        testImplementation("io.kotest:kotest-runner-junit5:4.+")
        testImplementation("io.kotest:kotest-assertions-core:4.+")
        testImplementation("io.mockk:mockk:1.10.6")
    }

    tasks {
        val jacocoTestReport by getting(JacocoReport::class)
        test {
            useJUnitPlatform()
            dependsOn(checkstyleMain, checkstyleTest)
            if (!System.getenv("CI").toBoolean()) {
                dependsOn(licenseFormat)
            }
            dependsOn(licenseMain, licenseTest)
            finalizedBy(jacocoTestReport)
        }

        jacocoTestReport {
            dependsOn(test)
            reports {
                xml.required.set(true)
                html.required.set(false)
            }
        }
    }

    indra {
        javaVersions {
            minimumToolchain(11)
            target(8)
        }

        publishReleasesTo("broccolai", "https://repo.broccol.ai/releases")
        publishSnapshotsTo("broccolai", "https://repo.broccol.ai/snapshots")
    }
}

// These are some options that either won't apply to rootProject, or
// will be nice to have to disable potential warnings and errors.
allprojects {
    repositories {
        mavenCentral()
    }

    extensions.configure(JavaPluginExtension::class) {
        disableAutoTargetJvm()
    }

    license {
        header = rootProject.file("LICENCE-HEADER")
        ext["year"] = Calendar.getInstance().get(Calendar.YEAR)
        include("**/*.java")
        include("**/*.kt")

        mapping("java", "DOUBLESLASH_STYLE")
    }

    checkstyle {
        toolVersion = "8.43"
        val configRoot = rootProject.projectDir.resolve(".checkstyle")
        configDirectory.set(configRoot)
        configProperties["basedir"] = configRoot.absolutePath
    }

    jacoco {
        reportsDirectory.set(rootProject.buildDir.resolve("reports").resolve("jacoco"))
    }

    tasks {
        compileJava {
            options.compilerArgs.add("-parameters")
        }

        compileTestJava {
            options.compilerArgs.add("-parameters")
        }

        compileKotlin {
            kotlinOptions.jvmTarget = "1.8"
            kotlinOptions.javaParameters = true
        }

        compileTestKotlin {
            kotlinOptions.jvmTarget = "1.8"
            kotlinOptions.javaParameters = true
        }
    }
}
