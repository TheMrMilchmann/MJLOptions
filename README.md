# MJL Options

[![License](https://img.shields.io/badge/license-Apache%202.0-yellowgreen.svg?style=flat-square)](https://github.com/TheMrMilchmann/MJLOptions/blob/master/LICENSE)
[![Build Status](https://img.shields.io/travis/TheMrMilchmann/MJLOptions.svg?style=flat-square)](https://travis-ci.org/TheMrMilchmann/MJLOptions)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.themrmilchmann.mjl/mjl-options.svg?style=flat-square&label=maven%20central)](https://maven-badges.herokuapp.com/maven-central/com.github.themrmilchmann.mjl/mjl-options)

A **m**inmal **J**ava **l**ibrary which provides a convenient way to parse command line parameters.

MJL Options comes with a custom specification for command line parameters that is meant to provide a sane and
predictable format.


## Specification

1. **Arguments**<br>
    Arguments are index-based interpreted parameters.

    An argument is "present" iff it has been discovered by the parser.

    1. **Optional Arguments**<br>
        Arguments may be denoted as "optional arguments" (optionally) with an associated default value. An optional
        argument is treated as a regular argument with the following exception:
        - An optional argument may not be present. (In this case if the argument has a default value, it is assigned as
        value for the argument.)
                
        Any argument with an index greater than an optional argument must also be optional.
    
    1. **Vararg Argument**<br>
        The trailing argument may be denoted as "vararg argument".<br>
        An indefinite amount of values may be assigned to a vararg argument. Vararg arguments may additionally be
        optional. If a vararg argument is not optional, at least one value must be explicitly assigned.

1. **Options**<br>
    Options are key-based interpreted parameters.<br>
    All options are uniquely identifiable by a case-sensitive alphanumeric key (`[A-Za-z]([A-Za-z0-9]|-)*`). These keys
    are also referred to as "long tokens" throughout this document.<br>
    Additionally it is possible to use a single alphabetic character as alternate case-sensitive key. These keys are
    also referred as "short tokens" throughout this document.
    
    An option is "present" iff it has been discovered by the parser.

    1. **Marker Options**<br>
        Options may be denoted as "marker options" with an associated marker value. A marker option is treated as a
        regular option with the following exceptions:
        - A marker option may be explicitly present without a specified value.

        Additionally marker options may be denoted to be usable only as markers (or "marker-only options"). A
        marker-only option is treated as a regular option with the following exceptions:
        - A marker-only option must either be explicitly present without a specified value or not be present at all.
    
1. **Parsing Rules**<br>

    1. **Interpreting Parameters**<br>
        A parameter is treated differently based on the preceding delimiter. Parameters are interpreted as:
        - **Option (by long token)** if the parameter is prefixed with a double-hyphen delimiter (`--`) and followed by
        any non-whitespace character,
        - **Parser escape symbol** if the parameter is equal to a double-hyphen delimiter (`--`),
        - **Option (by short token)** if the parameter is prefixed with a single-hyphen delimiter (`-`) and followed by
        an alphabetic character, or
        - **Argument** (or value depending on context) otherwise.

    1. **Parsing Options (by long token)**<br>
        A parameter prefixed with a double-hyphen delimiter (`--`) is interpreted as an option.
        All characters following the delimiter until either any whitespace character or an equals character (`=`) are
        interpreted as long token for the option.
    
        Parsing is continued as specified in _Parsing Option Values_.
    
    1. **Parsing Options (by short token)**<br>
        A parameter prefixed with a single-hyphen delimiter (`-`) is interpreted as an option or a bunch of options.
        All characters following the delimiter until either any whitespace character or an equals character (`=`) are
        interpreted as short tokens for the options.
        
        This definition allows multiple short tokens to be chained behind a single-hyphen delimiter. Thus, `-a -b -c`
        and `-abc` are equivalent. (Furthermore, `-a <value> -b <value> -c <value>` and `-abc <value>` are equivalent.)

        If all chained options have the same type, parsing is continued as specified in _Parsing Option Values_,
        otherwise the following rules apply:
        - When regular options and marker-only options are chained parsing fails.
        - When marker options and regular options are chained parsing continues as specified for regular options.
        - When marker options and marker-only options are chained parsing continues as specified for marker-only
        options.
        
    1. **Parsing Options Dynamically**<br>
        A parameter prefixed with a hyphen-dollar delimiter (`-$`) is interpreted as a dynamic option.
        All characters following the delimiter until either any whitespace character or an equals character (`=`) are
        interpreted as token for the option.
        
        Iff the token is followed immediately by an equals character the characters following this character are
        interpreted as value and parsed as specified in _Parsing Option Values_.

    1. **Parsing Option Values**<br>
        Parsing branches depending on the type of the option.
        - Regular option: The next parameter is parsed and interpreted as value for the option. (If no next parameter
        exists or the parameter is not a value, parsing fails.)
        - Marker option: If a next parameter exists and it is a value it is interpreted as value for the option.
        (Otherwise the marker value is assigned as value for the option and the next parameter is interpreted
        regularly.)        
        - Marker-only option: The next parameter is interpreted regularly.
        
    1. **Parsing Arguments and values**<br>
        TODO
    
    1. **Escaping Option Parsing**<br>
        All parameters following a parser escape symbol, a parameter consisting of just a double-hyphen delimiter
        (`--`), are interpreted as arguments.

## Grammar

### Notation
This section informally explains the grammar notation used below.

#### Symbols and naming
- Terminal symbol names start with an uppercase letter, e.g. `String`.
- Nonterminal symbol names start with lowercase letter, e.g. `command`.
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
command
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
    : "-$" LongOptionToken ("=" String)?
    ;

ShortOptionToken
    : <any alphabetic character>
    ;
    
LongOptionToken
    : <any alphanumeric character>+
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