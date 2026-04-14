import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

fun properties(key: String) = providers.gradleProperty(key)
fun environment(key: String) = providers.environmentVariable(key)

plugins {
    id("java") // Java support
    alias(libs.plugins.kotlin) // Kotlin support
    alias(libs.plugins.intellijPlatform) // IntelliJ Platform Gradle Plugin 2.x
    alias(libs.plugins.changelog) // Gradle Changelog Plugin
    alias(libs.plugins.qodana) // Gradle Qodana Plugin
    alias(libs.plugins.kover) // Gradle Kover Plugin
}

group = properties("pluginGroup").get()
version = properties("pluginVersion").get()

// Configure project's dependencies
repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create(properties("platformType").get(), properties("platformVersion").get())

        bundledPlugin("org.jetbrains.plugins.go")
        bundledPlugin("org.intellij.intelliLang")
        plugin("gherkin", properties("gherkinPluginVersion").get())

        pluginVerifier()
        zipSigner()

        testFramework(TestFrameworkType.Platform)
    }

    testImplementation("com.jetbrains.intellij.go:go-test-framework:${properties("goTestFrameworkVersion").get()}") {
        exclude("org.jetbrains.kotlin", "kotlin-stdlib-jdk8")
        exclude("org.jetbrains.kotlin", "kotlin-reflect")
        exclude("com.jetbrains.rd", "rd-core")
        exclude("com.jetbrains.rd", "rd-swing")
        exclude("com.jetbrains.rd", "rd-framework")
        exclude("org.jetbrains.teamcity", "serviceMessages")
        exclude("io.ktor", "ktor-network-jvm")
        exclude("com.jetbrains.infra", "download-pgp-verifier")
        exclude("ai.grazie.utils", "utils-common-jvm")
        exclude("ai.grazie.model", "model-common-jvm")
        exclude("ai.grazie.model", "model-gec-jvm")
        exclude("ai.grazie.model", "model-text-jvm")
        exclude("ai.grazie.nlp", "nlp-common-jvm")
        exclude("ai.grazie.nlp", "nlp-detect-jvm")
        exclude("ai.grazie.nlp", "nlp-langs-jvm")
        exclude("ai.grazie.nlp", "nlp-patterns-jvm")
        exclude("ai.grazie.nlp", "nlp-phonetics-jvm")
        exclude("ai.grazie.nlp", "nlp-similarity-jvm")
        exclude("ai.grazie.nlp", "nlp-stemmer-jvm")
        exclude("ai.grazie.nlp", "nlp-tokenizer-jvm")
        exclude("ai.grazie.spell", "hunspell-en-jvm")
        exclude("ai.grazie.spell", "gec-spell-engine-local-jvm")
        exclude("ai.grazie.utils", "utils-lucene-lt-compatibility-jvm")
    }
}

// Set the JVM language level used to build the project.
kotlin {
    jvmToolchain(21)
}

// Configure IntelliJ Platform Gradle Plugin 2.x
intellijPlatform {
    pluginConfiguration {
        name = properties("pluginName")
        version = properties("pluginVersion")

        ideaVersion {
            sinceBuild = properties("pluginSinceBuild")
            untilBuild = provider { null }
        }

        // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
        description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            val start = "<!-- Plugin description -->"
            val end = "<!-- Plugin description end -->"

            with(it.lines()) {
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
            }
        }

        val changelog = project.changelog // local variable for configuration cache compatibility
        // Get the latest available change notes from the changelog file
        changeNotes = properties("pluginVersion").map { pluginVersion ->
            with(changelog) {
                renderItem(
                    (getOrNull(pluginVersion) ?: getUnreleased())
                        .withHeader(false)
                        .withEmptySections(false),
                    Changelog.OutputType.HTML,
                )
            }
        }
    }

    signing {
        certificateChain = environment("CERTIFICATE_CHAIN")
        privateKey = environment("PRIVATE_KEY")
        password = environment("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = environment("PUBLISH_TOKEN")
        // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
        // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
        // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
        channels = properties("pluginVersion").map {
            listOf(
                it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" })
        }
    }

    pluginVerification {
        ides {
            recommended()
        }
    }

    buildSearchableOptions = false
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
    groups.empty()
    repositoryUrl = properties("pluginRepositoryUrl")
}

// Configure Gradle Kover Plugin - read more: https://github.com/Kotlin/kotlinx-kover#configuration
kover {
    reports {
        total {
            xml {
                onCheck = true
            }
        }
    }
}

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion").get()
    }
}
