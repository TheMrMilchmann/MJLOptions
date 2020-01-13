/*
 * Copyright 2018-2020 Leon Linhart
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
    `mjl-library`
}

val artifactName = "mjl-options"
    .also { extra["artifactName"] = it }

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

            artifactId = "mjl-options"

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
    isRequired = (deployment.type === com.github.themrmilchmann.build.BuildType.RELEASE)
    sign(publishing.publications)
}

dependencies {
    api(project(":modules.annotations", "archives")) // Uses the "archives" configuration to respect the MR module-descriptor
    compileOnly(group = "com.google.code.findbugs", name = "jsr305", version = "3.0.2")

    implementation(group = "net.bytebuddy", name= "byte-buddy-dep", version = "1.10.1")

    testImplementation(group = "org.testng", name = "testng", version = "6.14.3")
}