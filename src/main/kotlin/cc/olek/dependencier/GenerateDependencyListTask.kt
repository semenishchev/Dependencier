package cc.olek.dependencier

import groovy.lang.Closure
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.credentials.Credentials
import org.gradle.api.tasks.TaskAction
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.util.internal.ConfigureUtil
import java.io.File
import java.net.URI
import java.net.URL

open class GenerateDependencyListTask : DefaultTask() {

    @TaskAction
    fun generate() {
        val config = project.extensions.findByType(GenerateDependencyListExtension::class.java) ?: return
        for (task in project.tasks.withType(ProcessResources::class.java)) {
            val propertiesFile = File("${task.destinationDir}/BUILD-INF", "dependencies.txt")
            val repositoriesFile = File("${task.destinationDir}/BUILD-INF", "repositories.txt")
            propertiesFile.parentFile.mkdirs()

            propertiesFile.printWriter().use { writer ->
                for (configName in config.configurations) {
                    val configuration: Configuration? = project.configurations.findByName(configName)
                    configuration?.let {
                        for (dependency in it.allDependencies) {
                            writer.println("${dependency.group}:${dependency.name}:${dependency.version};")
                        }
                    }
                }
            }

            repositoriesFile.printWriter().use { writer ->
                for (repository in config.repos) {
                    val uri = repository.url
                    if(uri.scheme == "file") continue
                    if(uri.host == "repo.maven.apache.org") {
                        logger.warn("Using maven central as a CDN is not allowed. Use a mirror")
                        if(!project.hasProperty("iKnowWhatImDoing")) continue
                    }
                    val credentials = repository.credentials
                    var username = "\t"
                    var password = "\t"
                    if(credentials is PasswordCredentials) {
                        username = credentials.username ?: "\t"
                        password = credentials.password ?: "\t"
                    } else if(credentials != null) {
                        logger.warn("Credentials of type ${credentials.javaClass.name} are not supported")
                        continue
                    }
                    writer.println("${uri};$username;$password;")
                }
            }
        }
    }

    open class GenerateDependencyListExtension {
        internal val configurations = mutableListOf<String>()
        internal val repos = mutableListOf<RepositoryHandle>()
        fun configurations(vararg configs: String) {
            configurations.addAll(configs)
        }

        fun repository(handler: RepositoryHandle.() -> Unit) {
            val repositoryHandle = RepositoryHandle()
            handler.invoke(repositoryHandle)
            repos.add(repositoryHandle)
        }

        fun repository(closure: Closure<*>) {
            val repositoryHandle = RepositoryHandle()
            ConfigureUtil.configure(closure, repositoryHandle)
            repos.add(repositoryHandle)
        }
    }

    class RepositoryHandle {
        var url: URI = URI("https://localhost")
        var credentials: Credentials? = null
        fun url(url: Any?) {
            setUrl(url)
        }

        fun setUrl(url: Any?) {
            if (url == null) return
            this.url = when (url) {
                is URI -> url
                is URL -> url.toURI()
                else -> URI.create(url.toString())
            }
        }
    }
}
