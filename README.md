# MJL Options

[![License](https://img.shields.io/badge/license-Apache%202.0-yellowgreen.svg?style=flat-square)](https://github.com/TheMrMilchmann/MJLOptions/blob/master/LICENSE)
[![Build Status](https://img.shields.io/travis/TheMrMilchmann/MJLOptions/master.svg?style=flat-square)](https://travis-ci.org/TheMrMilchmann/MJLOptions)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.themrmilchmann.mjl/mjl-options.svg?style=flat-square&label=maven%20central)](https://maven-badges.herokuapp.com/maven-central/com.github.themrmilchmann.mjl/mjl-options)

A **m**inmal **J**ava **l**ibrary which provides a convenient way to parse command line parameters (for Java 8 and
later).

MJL Options is a Java implementation of the [sane-argv specification](https://github.com/TheMrMilchmann/sane-argv/).

[JavaDoc for MJL Options.](https://themrmilchmann.github.io/MJLOptions/)


## Installing

Running the build requires JDK 8 or later. Additionally, a local copy of JDK 9 is required. The buildscript is trying to
discover this copy by inspecting the following environment variables: `JDK9_HOME`, `JAVA9_HOME`, `JDK_19`, `JDK_9`.

Starting the actual build is simply a matter of invoking the respective Gradle tasks. For example: In order to run a
full build of the project, call

    ./gradlew build

Additionally, in order to reproduce snapshot and release builds it is required to supply the build with a an additional
parameter instead. This should generally be done on a per-build basis by adding `-Psnapshot` or `-Prelease` to the
command.


## License

Copyright 2018 Leon Linhart

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.