import org.gradle.internal.extensions.stdlib.toDefaultLowerCase

plugins {
    kotlin("jvm") version "2.0.21"
    `kotlin-dsl`
    `maven-publish`
}

group = "cc.olek"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
    testImplementation(kotlin("test"))
}

gradlePlugin {
    plugins {
        create("dependencier") {
            id = "cc.olek.dependencier"
            implementationClass = "cc.olek.dependencier.Dependencier"
        }
    }
}

publishing {
    repositories {
        maven {
            name = "repo"
            url = uri(rootProject.properties[name]?.toString() ?: "https://not.specified/")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "dependencier"
            version = project.version.toString()
            from(project.components["java"])
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}