package cc.olek.dependencier

import cc.olek.dependencier.GenerateDependencyListTask.GenerateDependencyListExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class Dependencier : Plugin<Project> {
    override fun apply(target: Project) {
        val task = target.tasks.register("generateDependencyList", GenerateDependencyListTask::class.java)
        target.tasks.named("processResources").orNull?.dependsOn(task)
        target.extensions.create("dependencyList", GenerateDependencyListExtension::class.java)
    }
}