# MJL Options

[![License](https://img.shields.io/badge/license-Apache%202.0-yellowgreen.svg?style=flat-square&label=License)](https://github.com/TheMrMilchmann/MJLOptions/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.themrmilchmann.mjl/mjl-options.svg?style=flat-square&label=Maven%20Central)](https://maven-badges.herokuapp.com/maven-central/com.github.themrmilchmann.mjl/mjl-options)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.themrmilchmann.mjl/mjl-options.svg?style=flat-square&label=JavaDoc&color=blue)](https://javadoc.io/doc/com.github.themrmilchmann.mjl/mjl-options)

A **m**inmal **J**ava **l**ibrary which provides a convenient way to parse command line parameters (for Java 8 and
later).

MJL Options is a Java implementation of the [sane-argv specification](https://github.com/TheMrMilchmann/sane-argv/).


## Building from source

### Setup

A complete build expects multiple JDK installations set up as follows:
1. JDK 1.8 (used to compile the basic library)
2. JDK   9 (used to compile the module descriptor)
3. JDK  13 (used to generate the JavaDoc)

These JDKs must be made visible to the build process by setting up
environment variables (or [Gradle properties](https://docs.gradle.org/current/userguide/build_environment.html#sec:gradle_configuration_properties))
for each JDK version as follows:

```
JAVA_HOME="path to JDK 1.8"
JDK_8="path to JDK 1.8"
JDK_9="path to JDK 9"
JDK_13="path to JDK 13"
```


### Building

Once the setup is complete, invoke the respective Gradle tasks using the
following command on Unix/macOS:

    ./gradlew <tasks>

or the following command on Windows:

    gradlew <tasks>

Important Gradle tasks to remember are:
- `clean`                   - clean build results
- `build`                   - assemble and test the Java library
- `publishToMavenLocal`     - build and install all public artifacts to the
                              local maven repository

Additionally `tasks` may be used to print a list of all available tasks.


## License

Copyright 2018-2020 Leon Linhart

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.