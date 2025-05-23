pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        maven(url ="https://oss.sonatype.org/content/repositories/snapshots/")
        maven(url ="https://jitpack.io")
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven(url ="https://oss.sonatype.org/content/repositories/snapshots/")
        maven(url ="https://jitpack.io")
        google()
        mavenCentral()
    }
}

rootProject.name = "xray-frontend"
include(":app")
