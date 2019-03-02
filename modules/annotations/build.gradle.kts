/*
 * Copyright 2018-2019 Leon Linhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.github.themrmilchmann.build.*

plugins {
    java
    signing
    `maven-publish`
}

val artifactName = "mjl-options-annotations"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
    val compileJava9 = create<JavaCompile>("compileJava9") {
        val jdk9Props = arrayOf(
            "JDK9_HOME",
            "JAVA9_HOME",
            "JDK_19",
            "JDK_9"
        )

        val ftSource = fileTree("src/main-jdk9/java")
        ftSource.include("**/*.java")
        options.sourcepath = files("src/main-jdk9/java")
        source = ftSource

        classpath = files()
        destinationDir = File(buildDir, "classes/java-9/main")

        sourceCompatibility = "9"
        targetCompatibility = "9"

        afterEvaluate {
            // module-path hack
            options.compilerArgs.add("--module-path")
            options.compilerArgs.add(compileJava.get().classpath.asPath)
        }

        val jdk9Home = jdk9Props.mapNotNull { System.getenv(it) }
            .map { File(it) }
            .firstOrNull(File::exists) ?: throw Error("Could not find valid JDK9 home")
        options.forkOptions.javaHome = jdk9Home
        options.isFork = true
    }

    "test"(Test::class) {
        useTestNG()
    }

    "jar"(Jar::class) {
        dependsOn(compileJava9)

        baseName = artifactName

        into("META-INF/versions/9") {
            from(compileJava9.outputs.files) {
                include("module-info.class")
            }
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
        baseName = artifactName
        classifier = "sources"
        from(sourceSets["main"].allSource)
    }

    val javadoc = "javadoc"(Javadoc::class)

    create<Jar>("javadocJar") {
        dependsOn(javadoc)

        baseName = artifactName
        classifier = "javadoc"
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
    (publications) {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            artifactId = artifactName

            pom {
                name.set(project.name)
                description.set("Marker annotations for OptionPool generation.")
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
    sign(publishing.publications)
}

val signMavenJavaPublication by tasks.getting {
    onlyIf { deployment.type === com.github.themrmilchmann.build.BuildType.RELEASE }
}