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

    // El host es público; la renovación no escribe credenciales en archivos versionados.
    val nexusCodeArtifactUrl = providers.gradleProperty("nexusCodeArtifactUrl")
        .orNull?.trim()?.takeIf(String::isNotEmpty)
        ?: "https://nexus-279391564691.d.codeartifact.us-east-2.amazonaws.com/maven/nexus-android/"

    val nexusCodeArtifactToken = if (gradle.startParameter.isOffline) {
        // En modo offline Gradle usa los artefactos locales sin contactar a AWS.
        providers.gradleProperty("nexusCodeArtifactToken").orNull ?: "offline-cache"
    } else {
        val host = java.net.URI(nexusCodeArtifactUrl).host.orEmpty()
        val repository = Regex("^(.+)-([0-9]{12})\\.d\\.codeartifact\\.([a-z0-9-]+)\\.amazonaws\\.com$")
            .matchEntire(host)
            ?: throw GradleException("nexusCodeArtifactUrl debe identificar un repositorio Maven de AWS CodeArtifact.")
        val (domain, owner, region) = repository.destructured

        // Android Studio abierto desde Finder puede no heredar el PATH de la terminal.
        val configuredAws = providers.gradleProperty("nexusAwsExecutable").orNull
            ?: providers.environmentVariable("AWS_CLI_EXECUTABLE").orNull
        val candidates = if (!configuredAws.isNullOrBlank()) {
            listOf(java.io.File(configuredAws))
        } else {
            val pathCandidates = providers.environmentVariable("PATH").orNull.orEmpty()
                .split(java.io.File.pathSeparator).filter(String::isNotBlank)
                .flatMap { directory -> listOf(java.io.File(directory, "aws"), java.io.File(directory, "aws.exe")) }
            pathCandidates + listOf(
                java.io.File("/usr/local/bin/aws"),
                java.io.File("/opt/homebrew/bin/aws"),
                java.io.File(System.getProperty("user.home"), ".local/bin/aws")
            )
        }
        val aws = candidates.firstOrNull { it.isFile && it.canExecute() }
            ?: throw GradleException(
                "No se encontró AWS CLI. Instálalo o configura nexusAwsExecutable con su ruta absoluta " +
                    "en el gradle.properties del usuario de Gradle."
            )
        val profile = providers.gradleProperty("nexusAwsProfile").orNull?.trim()?.takeIf(String::isNotEmpty)
            ?: providers.environmentVariable("AWS_PROFILE").orNull?.takeIf(String::isNotBlank)
            ?: providers.environmentVariable("AWS_DEFAULT_PROFILE").orNull?.takeIf(String::isNotBlank)
        val command = mutableListOf(
            aws.absolutePath, "codeartifact", "get-authorization-token",
            "--domain", domain, "--domain-owner", owner, "--region", region,
            "--duration-seconds", "43200", "--query", "authorizationToken", "--output", "text",
            "--no-cli-pager", "--cli-connect-timeout", "5", "--cli-read-timeout", "15"
        )
        if (profile != null) command.addAll(listOf("--profile", profile))

        // Provider.exec captura la salida: el token no se imprime ni se escribe en el repositorio.
        val authorization = providers.exec {
            commandLine(command)
            environment("AWS_PAGER", "")
            environment("AWS_CLI_AUTO_PROMPT", "off")
            environment("AWS_MAX_ATTEMPTS", "2")
            isIgnoreExitValue = true
        }
        if (authorization.result.get().exitValue != 0) {
            val diagnostic = authorization.standardError.asText.get()
            val login = "aws sso login" + (profile?.let { " --profile $it" } ?: "")
            val reason = when {
                diagnostic.contains("expired", ignoreCase = true) || diagnostic.contains("sso", ignoreCase = true) ->
                    "La sesión AWS no está vigente. Si usas SSO, ejecuta: $login"
                diagnostic.contains("Unable to locate credentials", ignoreCase = true) ->
                    "No hay credenciales AWS disponibles. Configura AWS CLI o selecciona nexusAwsProfile."
                diagnostic.contains("AccessDenied", ignoreCase = true) ->
                    "El perfil AWS necesita codeartifact:GetAuthorizationToken y sts:GetServiceBearerToken."
                else -> "AWS CLI no pudo emitir el token. Revisa el perfil AWS y la conexión."
            }
            throw GradleException("No se pudo renovar el acceso a Nexus CodeArtifact. $reason")
        }
        authorization.standardOutput.asText.get().trim().also { token ->
            if (token.isBlank() || token == "None" || token == "null" || token.any(Char::isWhitespace)) {
                throw GradleException("AWS CLI no devolvió un token válido para Nexus CodeArtifact.")
            }
        }
    }

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
