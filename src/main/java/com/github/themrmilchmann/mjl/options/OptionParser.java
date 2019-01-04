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
package com.github.themrmilchmann.mjl.options;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Option parser interface.
 *
 * @since   0.1.0
 *
 * @author  Leon Linhart
 */
public final class OptionParser {

    static final String REGEX_LONG_TOKEN = "[A-Za-z]([A-Za-z0-9]|-|\\.)*";

    /* Just used for parsing. This is not the actual spec compliant regex for options in general. */
    private static final String REGEX_OPTION = "(?:--|-#|-)(" + REGEX_LONG_TOKEN + ")(?:=(.*))?";
    private static final Pattern PATTERN_OPTION = Pattern.compile(REGEX_OPTION);

    /**
     * Parses parameters from an array of fragments.
     *
     * @param pool      the pool of available parameters
     * @param command   the command to be parsed
     *
     * @return  an immutable set of parsed values
     *
     * @since   0.1.0
     */
    public static OptionSet parseFragments(OptionPool pool, String... command) {
        OptionParser parser = new OptionParser(pool, command);
        parser.parse();

        Map<Object, Object> values = Collections.unmodifiableMap(parser.values);
        Map<String, String> dynamics = Collections.unmodifiableMap(parser.dynamics);

        return new OptionSet(pool, values, dynamics);
    }

    /**
     * {@link #lineToFragments(String) Converts the given line to fragments} and
     * {@link #parseFragments(OptionPool, String...) parses} those.
     *
     * @param pool  the pool of available parameters
     * @param line  the line to be parsed
     *
     * @return  an immutable set of parsed values
     *
     * @since   0.3.0
     */
    public static OptionSet parseLine(OptionPool pool, String line) {
        return parseFragments(pool, lineToFragments(line));
    }

    /**
     * Converts the given line to an array of fragments.
     *
     * <p>Lines are converted to fragments using the following rules:</p>
     * <ul>
     * <li>Parameters are delimited by whitespace characters.</li>
     * <li>A string surrounded by quotation marks (") is interpreted as single parameter, regardless of any whitespace
     * character contained within. (A quoted string may be embedded in an argument.)</li>
     * <li>A quotation mark preceded by a backslash (\") is interpreted as literal quotation mark character.</li>
     * </ul>
     *
     * @param line  the line to be converted
     *
     * @return  the array of fragments
     *
     * @since   0.3.0
     */
    public static String[] lineToFragments(String line) {
        // https://github.com/Project-Skara/jdk/blob/master/src/java.base/windows/native/libjli/cmdtoargs.c
        List<String> argList = new ArrayList<>();
        char[] cmd = line.toCharArray();

        for (int i = 0; i < cmd.length; i++) {
            StringBuilder argBuilder = new StringBuilder();
            char prev = '\0';
            int quotes = 0, slashes = 0;

            if (Character.isWhitespace(cmd[i])) {
                boolean inParam = false;

                for (; i < cmd.length; i++) {
                    if (Character.isWhitespace(cmd[i])) continue;

                    inParam = true;
                    break;
                }

                if (!inParam) break;
            }

            param: for (; i < cmd.length; i++) {
                switch (cmd[i]) {
                    case '"': {
                        if (prev == '\\') {
                            for (int j = 1; j < slashes; j += 2) {
                                argBuilder.append(prev);
                            }

                            if (slashes % 2 == 1) {
                                argBuilder.append(cmd[i]);
                            } else {
                                quotes++;
                            }
                        } else if (prev == '"' && quotes % 2 == 0) {
                            quotes++;
                            argBuilder.append(cmd[i]);
                        } else if (quotes == 0) {
                            quotes++; // starting quote
                        } else {
                            quotes--; // matching quote
                        }

                        slashes = 0;
                    } break;
                    case '\\': {
                        slashes++;
                    } break;
                    default: {
                        if (Character.isWhitespace(cmd[i])) {
                            if (quotes % 2 == 1) {
                                argBuilder.append(cmd[i]);
                            } else {
                                break param;
                            }
                        } else if (prev == '\\') {
                            for (int j = 0; j < slashes; j++) {
                                argBuilder.append(prev);
                            }

                            argBuilder.append(cmd[i]);
                        } else {
                            argBuilder.append(cmd[i]);
                        }

                        slashes = 0;
                    }
                }

                prev = cmd[i];
            }

            argList.add(argBuilder.toString());
        }

        return argList.toArray(new String[0]);
    }

    private final Map<Object, Object> values = new HashMap<>();
    private final Map<String, String> dynamics = new HashMap<>();

    private final OptionPool pool;
    private final String[] parameters;

    private OptionParser(OptionPool pool, String[] parameters) {
        this.pool = pool;
        this.parameters = parameters;
    }

    private void parse() {
        boolean ignoreOptions = false;
        List<Object> varargValues = null;
        int argIndex = 0;

        for (int curIndex = 0; curIndex < this.parameters.length; curIndex++) {
            String parameter = this.parameters[curIndex];
            ParameterType type = this.parseParameterType(parameter, ignoreOptions);

            if (type == ParameterType.ARGUMENT) {
                if (!this.pool.hasArgument(argIndex)) throw new ParsingException("No argument with index " + argIndex + " available in " + this.pool);

                Argument<?> arg = this.pool.getArgument(argIndex);
                String rawValue = this.parameters[curIndex];
                Object value = arg.parser.parse(rawValue);

                if (this.pool.getLastArgument() == arg && this.pool.isLastVararg()) {
                    if (varargValues == null) {
                        varargValues = new ArrayList<>();
                        this.values.put(arg, varargValues);
                    }

                    varargValues.add(value);
                } else {
                    this.values.put(arg, value);
                    argIndex++;
                }
            } else if (type.isOption()) {
                Matcher optionMatcher = PATTERN_OPTION.matcher(parameter);
                if (!optionMatcher.matches()) throw new ParsingException("Illegal option format: " + parameter);

                String tokens = optionMatcher.group(1);
                String rawValue = optionMatcher.group(3);

                if (type == ParameterType.OPTION_BY_LONG_TOKEN) {
                    Option<?> opt = this.pool.getOption(tokens);

                    if (opt == null) throw new ParsingException("Unknown long token '" + tokens + "'.");
                    if (this.values.containsKey(opt)) throw new ParsingException("Duplicate option " + opt + ".");
                    if (opt.isMarkerOnly() && rawValue != null) throw new ParsingException("Specified value for marker-only option in fragment '" + parameter + "'.");

                    if (!opt.isMarkerOnly() && rawValue == null && !parameter.endsWith("=") && curIndex + 1 < this.parameters.length) {
                        String nextParameter = this.parameters[curIndex + 1];
                        ParameterType nextType = this.parseParameterType(nextParameter, ignoreOptions);

                        if (nextType == ParameterType.ARGUMENT) {
                            curIndex++;
                            rawValue = nextParameter;
                        }
                    }

                    if (opt.isMarkerOnly() || (opt.hasMarkerValue() && (rawValue == null))) {
                        this.values.put(opt, opt.getMarkerValue());
                    } else if (rawValue == null) {
                        throw new ParsingException("No value specified for fragment '--" + tokens + "'.");
                    } else {
                        Object value = opt.parser.parse(rawValue);
                        this.values.put(opt, value);
                    }
                } else if (type == ParameterType.OPTION_BY_SHORT_TOKEN) {
                    List<Option<?>> opts = new ArrayList<>(tokens.length());

                    for (char token : tokens.toCharArray()) {
                        Option<?> opt = this.pool.getOption(token);
                        if (opt == null) throw new ParsingException("Unknown short token '" + token + "'.");
                        if (this.values.containsKey(opt) || opts.contains(opt)) throw new ParsingException("Duplicate option " + opt + ".");
                        if (opt.isMarkerOnly() && rawValue != null) throw new ParsingException("Specified value for marker-only option in fragment '" + parameter + "'.");

                        opts.add(opt);
                    }

                    if (opts.stream().anyMatch(opt -> !opt.hasMarkerValue()) && opts.stream().anyMatch(Option::isMarkerOnly))
                        throw new ParsingException("Regular options and marker-only options may not be chained!");

                    if (opts.stream().noneMatch(Option::isMarkerOnly) && rawValue == null && !parameter.endsWith("=") && curIndex + 1 < this.parameters.length) {
                        String nextParameter = this.parameters[curIndex + 1];
                        ParameterType nextType = this.parseParameterType(nextParameter, ignoreOptions);

                        if (nextType == ParameterType.ARGUMENT) {
                            curIndex++;
                            rawValue = nextParameter;
                        }
                    }

                    if (opts.stream().allMatch(Option::isMarkerOnly) || (opts.stream().allMatch(Option::hasMarkerValue) && (rawValue == null))) {
                        opts.forEach(opt -> this.values.put(opt, opt.getMarkerValue()));
                    } else if (rawValue == null) {
                        throw new ParsingException("No value specified for fragment '-" + tokens + "'.");
                    } else {
                        for (Option<?> opt : opts) {
                            Object value = opt.parser.parse(rawValue);
                            this.values.put(opt, value);
                        }
                    }
                } else if (type == ParameterType.WILDCARD_OPTION) {
                    if (this.dynamics.containsKey(tokens)) throw new ParsingException("Duplicate dynamic option " + tokens + ".");

                    if (rawValue == null && !parameter.endsWith("=") && curIndex + 1 < this.parameters.length) {
                        String nextParameter = this.parameters[curIndex + 1];
                        ParameterType nextType = this.parseParameterType(nextParameter, ignoreOptions);

                        if (nextType == ParameterType.ARGUMENT) {
                            curIndex++;
                            rawValue = nextParameter;
                        }
                    }

                    this.dynamics.put(tokens, rawValue);
                } else {
                    throw new IllegalStateException("Internal parser error: Unhandled option parameter type");
                }
            } else if (type == ParameterType.ESCAPE_OPTION_PARSING) {
                ignoreOptions = true;
            }
        }
    }

    private ParameterType parseParameterType(String parameter, boolean ignoreOptions) {
        if (parameter.equals("--")) {
            return ParameterType.ESCAPE_OPTION_PARSING;
        } else if (!ignoreOptions) {
            if (parameter.startsWith("--")) {
                return ParameterType.OPTION_BY_LONG_TOKEN;
            } else if (parameter.startsWith("-#")) {
                return ParameterType.WILDCARD_OPTION;
            } else if (parameter.matches("-[^0.9].*")) {
                return ParameterType.OPTION_BY_SHORT_TOKEN;
            }
        }

        return ParameterType.ARGUMENT;
    }

    private enum ParameterType {
        ARGUMENT,
        OPTION_BY_LONG_TOKEN,
        OPTION_BY_SHORT_TOKEN,
        WILDCARD_OPTION,
        ESCAPE_OPTION_PARSING;

        boolean isOption() {
            return this == OPTION_BY_LONG_TOKEN || this == OPTION_BY_SHORT_TOKEN || this == WILDCARD_OPTION;
        }

    }

}