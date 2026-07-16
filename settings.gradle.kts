// Path: Paguito/settings.gradle.kts

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }

        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(
        RepositoriesMode.FAIL_ON_PROJECT_REPOS
    )

    val nexusCodeArtifactUrl = providers
        .gradleProperty("nexusCodeArtifactUrl")
        .orNull
        ?.trim()
        ?.takeIf(String::isNotEmpty)
        ?: error(
            """
            No se encontró nexusCodeArtifactUrl.

            Ejecuta:
            nexus-login
            """.trimIndent()
        )

    val nexusCodeArtifactToken = providers
        .gradleProperty("nexusCodeArtifactToken")
        .orNull
        ?.trim()
        ?.takeIf(String::isNotEmpty)
        ?: error(
            """
            No se encontró nexusCodeArtifactToken.

            Ejecuta:
            nexus-login
            """.trimIndent()
        )

    repositories {
        google()
        mavenCentral()

        maven {
            name = "NexusCodeArtifact"

            url = uri(
                nexusCodeArtifactUrl
            )

            credentials {
                username = "aws"
                password = nexusCodeArtifactToken
            }

            content {
                includeGroup(
                    "com.nexusecosystem"
                )
            }
        }
    }
}

rootProject.name = "Paguito"

include(":app")