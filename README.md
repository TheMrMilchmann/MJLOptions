# MJL Options

[![License](https://img.shields.io/badge/license-Apache%202.0-yellowgreen.svg?style=flat-square)](https://github.com/TheMrMilchmann/MJLOptions/blob/master/LICENSE)
[![Build Status](https://img.shields.io/travis/TheMrMilchmann/MJLOptions.svg?style=flat-square)](https://travis-ci.org/TheMrMilchmann/MJLOptions)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.themrmilchmann.mjl/mjl-options.svg?style=flat-square&label=maven%20central)](https://maven-badges.herokuapp.com/maven-central/com.github.themrmilchmann.mjl/mjl-options)

A **m**inmal **J**ava **l**ibrary which provides a convenient way to parse command line parameters (for Java 8 and
later).

MJL Options comes with a custom specification for command line parameters that is meant to provide a sane and
predictable format.

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


## Specification

1. **Definition**<br>

    1. **Arguments**<br>
        Arguments are index-based interpreted parameters.

        1. **Optional arguments**<br>
            Arguments may be denoted as _optional arguments_.

            - Optional arguments do not require a value to be specified.
            - All arguments required after an optional argument must also be optional.

        1. **Vararg arguments**<br>
            The trailing argument may be denoted as _vararg argument_.

            - An indefinite amount of values may be assigned to a vararg argument.
            - Vararg arguments may additionally be optional. (If a vararg argument is not optional, at least one value
                must be explicitly assigned.)

    1. **Options**<br>
        Options are key-based interpreted parameters.

        All options are uniquely identifiable by a case-sensitive key. These keys are also referred to as _long tokens_
        throughout this document. The key must match the pattern: `[A-Za-z]([A-Za-z0-9]|-|\.)*`

        Additionally it is possible to use a single alphabetic character as alternate case-sensitive key. These keys are
        also referred as _short tokens_ throughout this document.

        By default, a option are known to the parser ahead of time and require a value to be specified. Options that use
        this default behavior are also referred to as _regular options_ when necessary.

        1. **Marker options**<br>
            Options may be denoted as _marker options_.

            - A marker option allows for (but does not require) a value to be specified.

        1. **Marker-only options**<br>
            Options may be denoted as _marker-only options_.

            - A Marker-only option does not allow for a value to be specified.

        1. **Wildcard options**<br>
            Values may be specified for options that are not known ahead of time. These options are referred to as
            _wildcard options_.

            For the purpose of parsing, wildcard options are treated like marker options.

1. **Parsing**<br>
    Libraries implementing this specification must provide at least two different ways to parse user input: parsing
    coherent character sequences (e.g. Strings) which are referred to as _lines_ and parsing pre-split collections of
    character sequences (e.g. Arrays of Strings) which are referred to as _fragments_.

    1. **Converting lines to fragments**<br>
        TODO...

    1. **Interpreting Fragments**<br>
        Fragments are handled differently depending on the current state of the parser and their initial characters.
        A fragment is interpreted as...

        1. **option escape sequence** if the fragment is `--` (if this cases is matched for the first time, option
            parsing is terminated, otherwise an error is thrown).
        1. **argument** if option parsing has been terminated.
        1. **option** (identified by a long token) if the fragment is prefixed by a double-hyphen delimiter (`--`).
        1. **wildcard option** if the fragment is prefixed by a hyphen-route delimiter (`-#`).
        1. **option/s** (identified by short token/s) if the fragment is prefixed by a single-hyphen delimiter which is
            not followed by a digit.
        1. **argument**.

    1. **Parsing options identified by long tokens**<br>
        All characters following the initial double-hyphen delimiter until either an equals sign (`=`) or the end of the
        fragment are interpreted as long token for the option.

        If the long token is suffixed by an equals sign, all characters following that equals sign until the end of the
        fragment are interpreted as value for the option. If the option requires a value to be specified, or the option
        allows for a value to be specified and the next fragment would be interpreted as an argument, the next fragment
        is interpreted as value for the option. Otherwise, the next fragment is interpreted regularly.
        
    1. **Parsing wildcard options**<br>
        All characters following the `-$` delimiter until either an equals sign (`=`) or the end of the fragment are
        interpreted as long token for the option.

        If the wildcard token is suffixed by an equals sign, all characters following that equals sign until the end of
        the fragment are interpreted as value for the option. If the next fragment would be interpreted as an argument,
        it is interpreted as value for the option. Otherwise, the next fragment is interpreted regularly.

    1. **Parsing options identified by short tokens**<br>
        All characters following the initial hyphen delimiter until either an equals sign or the end of the fragment are
        interpreted as short tokens for the options.

        This definition allows for multiple options to be bundled together.  Thus, `-a -b -c` and `-abc` are equivalent.
        (Furthermore, `-a <value> -b <value> -c <value>` and `-abc <value>` are equivalent.)

        If the short tokens are suffixed by an equals sign, all characters following that equals sign until the end of
        the fragment are interpreted as value for the options. If any option require a value to be specified, the next
        fragment is interpreted as value for the options. If any option does not allow for a value to be specified, the
        next fragment is interpreted regularly.

    1. **Parsing arguments**<br>
        The fragment is interpreted as value for the current argument.


## Grammar

### Notation
This section informally explains the grammar notation used below.

#### Symbols and naming
- Terminal symbol names start with an uppercase letter, e.g. `String`.
- Nonterminal symbol names start with lowercase letter, e.g. `line`.
- Each production starts with a colon (:).
- Symbol definitions may have many productions and are terminated by a semicolon (;).
- Symbol definitions may be prepended with attributes, e.g. start attribute denotes a start symbol.

#### EBNF expressions
- Operator `|` denotes *alternative*.
- Operator `*` denotes *iteration* (zero or more).
- Operator `+` denotes *iteration* (one or more).
- Operator `?` denotes *option* (zero or one).

### Syntax

```
start
line
    : parameter* ("--" String*)?
    ;

parameter
    : String
    : option
    ;
    
option
    : simpleOption
    : markerOption
    : markerOnlyOption
    : dynamicOption
    ;
    
simpleOption
    : "-" ShortOptionToken+ <any whitespace character>+ String
    : "-" ShortOptionToken+ "=" String
    : "--" LongOptionToken <any whitespace character>+ String
    : "--" LongOptionToken "=" String
       ;

markerOnlyOption
    : "-" ShortOptionToken+
    : "--" LongOptionToken
    ;

markerOption
    : simpleOption
    : markerOnlyOption
    ;

dynamicOption
    : "-#" LongOptionToken ("=" String)?
    ;

ShortOptionToken
    : <any valid character for short tokens>
    ;
    
LongOptionToken
    : <any valid character for long tokens>+
    ;

String
    : <any non-whitespace character>+
    : "\"" <any character (possibly backslash-escaped)>? "\""
    ;
```


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