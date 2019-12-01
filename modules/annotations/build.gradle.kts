/*
 * Copyright 2018-2019 Leon Linhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.github.themrmilchmann.build.*
import org.gradle.internal.jvm.*

plugins {
    `java-library`
    signing
    `maven-publish`
}

val artifactName = "mjl-options-annotations"
val currentJVMAtLeast9 = Jvm.current().javaVersion!! >= JavaVersion.VERSION_1_9

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    compileJava {
        /* JDK 8 does not support the --release option */
        if (Jvm.current().javaVersion!! > JavaVersion.VERSION_1_8) {
            // Workaround for https://github.com/gradle/gradle/issues/2510
            options.compilerArgs.addAll(listOf("--release", "8"))

            /*
             * There is a bug in JDK 9 preventing usage of the --release option from Gradle, thus the process needs to
             * be run in a fork. (https://bugs-stage.openjdk.java.net/browse/JDK-8139607)
             */
            if (Jvm.current().javaVersion!! == JavaVersion.VERSION_1_9) {
                options.isFork = true
                options.forkOptions.javaHome = Jvm.current().javaHome!!
            }
        }
    }

    val compileJava9 = create<JavaCompile>("compileJava9") {
        val ftSource = fileTree("src/main/java-jdk9")
        ftSource.include("**/*.java")
        options.sourcepath = files("src/main/java-jdk9")
        source = ftSource

        classpath = files()
        destinationDir = File(buildDir, "classes/java-jdk9/main")

        sourceCompatibility = "9"
        targetCompatibility = "9"

        /*
         * There is a bug in JDK 9 preventing usage of the --release option from Gradle. Luckily there is need no need
         * to specify --release 9 when we are running on JDK9. (https://bugs-stage.openjdk.java.net/browse/JDK-8139607)
         */
        if (Jvm.current().javaVersion!! != JavaVersion.VERSION_1_9) {
            // Workaround for https://github.com/gradle/gradle/issues/2510
            options.compilerArgs.addAll(listOf("--release", "9"))
        }

        afterEvaluate {
            // module-path hack
            options.compilerArgs.add("--module-path")
            options.compilerArgs.add(compileJava.get().classpath.asPath)
        }

        /*
         * If the JVM used to invoke Gradle is JDK 9 or later, there is no reason to require a separate JDK 9 instance.
         */
        if (!currentJVMAtLeast9) {
            val jdk9Props = arrayOf(
                "JDK9_HOME",
                "JAVA9_HOME",
                "JDK_19",
                "JDK_9"
            )

            val jdk9Home = jdk9Props.mapNotNull { System.getenv(it) }
                .map { File(it) }
                .firstOrNull(File::exists) ?: throw Error("Could not find valid JDK9 home")
            options.forkOptions.javaHome = jdk9Home
            options.isFork = true
        }
    }

    classes {
        dependsOn(compileJava9)
    }

    jar {
        archiveBaseName.set(artifactName)

        into("META-INF/versions/9") {
            from(compileJava9.outputs.files.filter(File::isDirectory)) {
                exclude("**/Stub.class")
            }

            includeEmptyDirs = false
        }

        manifest {
            attributes(mapOf(
                "Name" to project.name,
                "Specification-Version" to project.version,
                "Specification-Vendor" to "Leon Linhart <themrmilchmann@gmail.com>",
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "Leon Linhart <themrmilchmann@gmail.com>",
                "Multi-Release" to "true"
            ))
        }
    }

    create<Jar>("sourcesJar") {
        archiveBaseName.set(artifactName)
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)

        into("META-INF/versions/9") {
            from(compileJava9.inputs.files.filter(File::isDirectory)) {
                exclude("**/Stub.java")
            }

            includeEmptyDirs = false
        }
    }

    javadoc {
        with (options as StandardJavadocDocletOptions) {
            tags = listOf(
                "apiNote:a:API Note:",
                "implSpec:a:Implementation Requirements:",
                "implNote:a:Implementation Note:"
            )
        }
    }

    create<Jar>("javadocJar") {
        dependsOn(javadoc)

        archiveBaseName.set(artifactName)
        archiveClassifier.set("javadoc")
        from(javadoc.get().outputs)
    }
}

publishing {
    repositories {
        maven {
            url = uri(deployment.repo)

            credentials {
                username = deployment.user
                password = deployment.password
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            artifactId = artifactName

            pom {
                name.set(project.name)
                description.set("Marker annotations for configuring MJLOptions dynamically.")
                packaging = "jar"
                url.set("https://github.com/TheMrMilchmann/MJLOptions")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://github.com/TheMrMilchmann/MJLOptions/blob/master/LICENSE")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("TheMrMilchmann")
                        name.set("Leon Linhart")
                        email.set("themrmilchmann@gmail.com")
                        url.set("https://github.com/TheMrMilchmann")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/TheMrMilchmann/MJLOptions.git")
                    developerConnection.set("scm:git:git://github.com/TheMrMilchmann/MJLOptions.git")
                    url.set("https://github.com/TheMrMilchmann/MJLOptions.git")
                }
            }
        }
    }
}

signing {
    isRequired = (deployment.type === com.github.themrmilchmann.build.BuildType.RELEASE)
    sign(publishing.publications)
}