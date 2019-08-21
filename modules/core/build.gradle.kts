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

plugins {
    `java-library`
    signing
    `maven-publish`
}

val artifactName = "mjl-options"

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

        val ftSource = fileTree("src/main/java-jdk9")
        ftSource.include("**/*.java")
        options.sourcepath = files("src/main/java-jdk9")
        source = ftSource

        classpath = files()
        destinationDir = File(buildDir, "classes/java-jdk9/main")

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

        options.encoding = "utf-8"
    }

    test {
        useTestNG()
    }

    jar {
        dependsOn(compileJava9)

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
                description.set("A minimal Java library which provides a convenient way to parse command line parameters.")
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
    isRequired = deployment.type === com.github.themrmilchmann.build.BuildType.RELEASE
    sign(publishing.publications)
}

dependencies {
    api(project(":modules.annotations"))
    compileOnly(group = "com.google.code.findbugs", name = "jsr305", version = "3.0.2")

    implementation(group = "net.bytebuddy", name= "byte-buddy-parent", version = "1.10.1")

    testCompile(group = "org.testng", name = "testng", version = "6.14.3")
}