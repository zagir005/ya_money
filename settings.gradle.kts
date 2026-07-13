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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ya-money"
include(":app")
include(":feature:accounts")
include(":feature:transactions")
include(":feature:transactions")
include(":core:ui")
include(":core:systemdesign")
include(":core:domain")
include(":core:data")
include(":finance:transactions")
include(":finance:accounts")
