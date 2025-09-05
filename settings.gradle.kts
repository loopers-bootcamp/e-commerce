rootProject.name = "e-commerce"

registerSubDirectoriesAsModules("apps", "modules", "supports")

// configurations
pluginManagement {
    val springBootVersion: String by settings
    val springDependencyManagementVersion: String by settings

    repositories {
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }
        gradlePluginPortal()
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "org.springframework.boot" -> useVersion(springBootVersion)
                "io.spring.dependency-management" -> useVersion(springDependencyManagementVersion)
            }
        }
    }
}

fun registerSubDirectoriesAsModules(vararg parentDirs: String) {
    parentDirs.forEach { parentDir ->
        file(parentDir)
            .listFiles { file -> file.isDirectory }
            ?.forEach { subDir ->
                val path = ":${parentDir}:${subDir.name}"

                include(path)
                project(path).projectDir = subDir
            }
    }
}
