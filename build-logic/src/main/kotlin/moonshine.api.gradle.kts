import com.adarshr.gradle.testlogger.theme.ThemeType
import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("moonshine.publishing")
    id("net.kyori.indra")
    id("net.kyori.indra.checkstyle")
    id("net.kyori.indra.license-header")
    id("com.adarshr.test-logger")
    java
    `java-library`
    jacoco
}

testlogger {
    theme = ThemeType.MOCHA_PARALLEL
    showPassed = true
}

configurations {
    testCompileClasspath {
        exclude(group = "junit")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    val libs = (project as ExtensionAware).extensions.getByName("libs") as LibrariesForLibs
    api(libs.checkerframework)

    testImplementation(libs.bundles.testing.api)
    testRuntimeOnly(libs.bundles.testing.runtime)
}

java {
    disableAutoTargetJvm()
}

tasks {
    withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
    }

    compileTestJava {
        options.release.set(11)
        sourceCompatibility = "11"
        targetCompatibility = sourceCompatibility
    }

    javadoc {
        val opt = options as StandardJavadocDocletOptions
        opt.addStringOption("Xdoclint:none", "-quiet")

        opt.encoding("UTF-8")
        opt.charSet("UTF-8")
        opt.source("8")
        doFirst {
            opt.links(
                "https://docs.oracle.com/javase/8/docs/api/"
            )
        }
    }
}
