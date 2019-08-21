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
rootProject.name = "MJL Options"

enableFeaturePreview("GRADLE_METADATA")

fun hasBuildscript(it: File) = File(it, "build.gradle.kts").exists()

fun File.discoverProjects(name: String = "") {
    val projectName = "$name${this.name}"

    if (hasBuildscript(this)) {
        include(projectName)
        project(":$projectName").projectDir = this
    }

    this.listFiles().filter(File::isDirectory).forEach {
        it.discoverProjects("$projectName.")
    }
}

file("modules").discoverProjects()
//file("plugins").discoverProjects()