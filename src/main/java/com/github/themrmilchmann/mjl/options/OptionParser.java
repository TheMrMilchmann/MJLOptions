/*
 * Copyright 2018 Leon Linhart
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
import javax.annotation.Nullable;

/**
 * Option parser interface.
 *
 * @since   0.1.0
 *
 * @author  Leon Linhart
 */
public final class OptionParser {

    /* Just used for parsing. This is not the actual spec compliant regex for options in general. */
    private static final String REGEX_OPTION = "(?:--|-\\$|-)([A-Za-z]([A-Za-z0-9]|-|\\.)*)(?:=(.*))?";
    private static final Pattern PATTERN_OPTION = Pattern.compile(REGEX_OPTION);

    /**
     * TODO doc
     *
     * @param pool      the pool of available parameters
     * @param command   the command to be parsed
     *
     * @return  an immutable set of parsed values
     *
     * @since   0.1.0
     */
    public static OptionSet parse(OptionPool pool, String... command) {
        OptionParser parser = new OptionParser(pool, command);
        parser.parse();

        Map<Object, Object> values = Collections.unmodifiableMap(parser.values);
        Map<String, String> dynamics = Collections.unmodifiableMap(parser.dynamics);

        return new OptionSet(pool, values, dynamics);
    }

    private final Map<Object, Object> values = new HashMap<>();
    private final Map<String, String> dynamics = new HashMap<>();

    private final OptionPool pool;
    private final String[] command;

    private int curIndex;

    @Nullable
    private List<Object> varargValues;
    private int argIndex = 0;

    private OptionParser(OptionPool pool, String[] command) {
        this.pool = pool;
        this.command = command;
    }

    private void parse() {
        boolean ignoreOptions = false;

        for (this.curIndex = 0; this.curIndex < this.command.length; this.curIndex++) {
            if (ignoreOptions) {
                this.parseArgument();
            } else {
                String fragment = this.command[this.curIndex];

                if (fragment.equals("--")) {
                    if (!ignoreOptions) {
                        ignoreOptions = true;
                    } else {
                        throw new ParsingException("Duplicate '--' escape sequence. Options parsing may only be terminated once!");
                    }
                } else {
                    Matcher optMatcher = PATTERN_OPTION.matcher(fragment);
                    if (!optMatcher.matches()) throw new ParsingException(); // TODO err

                    String tokens = optMatcher.group(1);
                    String rawValue = optMatcher.group(3);

                    if (fragment.startsWith("--")) {
                        Option<?> opt = this.pool.getOption(tokens);

                        if (opt == null) throw new ParsingException("Unknown long token '" + tokens + "'.");
                        if (this.values.containsKey(opt)) throw new ParsingException("Duplicate option " + opt + ".");
                        if (opt.isMarkerOnly() && rawValue != null) throw new ParsingException("Specified value for marker-only option in fragment '" + fragment + "'.");

                        if (!opt.isMarkerOnly() && rawValue == null && !fragment.endsWith("=") && this.curIndex + 1 < this.command.length) {
                            this.curIndex++;
                            rawValue = this.command[this.curIndex];
                        }

                        //noinspection DuplicateCondition
                        if (opt.isMarkerOnly() || (opt.hasMarkerValue() && (rawValue == null))) {
                            this.values.put(opt, opt.getMarkerValue());
                        } else if (rawValue == null) {
                            throw new ParsingException("No value specified for fragment '--" + tokens + "'.");
                        } else {
                            Object value = opt.parser.parse(rawValue);
                            this.values.put(opt, value);
                        }
                    } else if (fragment.startsWith("-$")) {
                        if (this.dynamics.containsKey(tokens)) throw new ParsingException("Duplicate dynamic option " + tokens + ".");

                        this.dynamics.put(tokens, rawValue);
                    } else if (fragment.startsWith("-")) {
                        List<Option<?>> opts = new ArrayList<>(tokens.length());

                        for (char token : tokens.toCharArray()) {
                            Option<?> opt = this.pool.getOption(token);
                            if (opt == null) throw new ParsingException("Unknown short token '" + token + "'.");
                            if (this.values.containsKey(opt) || opts.contains(opt)) throw new ParsingException("Duplicate option " + opt + ".");
                            if (opt.isMarkerOnly() && rawValue != null) throw new ParsingException("Specified value for marker-only option in fragment '" + fragment + "'.");

                            opts.add(opt);
                        }

                        if (opts.stream().anyMatch(opt -> !opt.hasMarkerValue()) && opts.stream().anyMatch(Option::isMarkerOnly))
                            throw new ParsingException("Regular options and marker-only options may not be chained!");

                        if (opts.stream().noneMatch(Option::isMarkerOnly) && rawValue == null && !fragment.endsWith("=") && this.curIndex + 1 < this.command.length) {
                            this.curIndex++;
                            rawValue = this.command[this.curIndex];
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
                    } else {
                        this.parseArgument();
                    }
                }
            }
        }
    }

    private void parseArgument() {
        if (!this.pool.hasArgument(this.argIndex)) throw new ParsingException();

        Argument<?> arg = this.pool.getArgument(this.argIndex);
        String rawValue = this.command[this.curIndex];
        Object value = arg.parser.parse(rawValue);

        if (this.pool.getLastArgument() == arg && this.pool.isLastVararg()) {
            if (this.varargValues == null) {
                this.varargValues = new ArrayList<>();
                this.values.put(arg, this.varargValues);
            }

            this.varargValues.add(value);
        } else {
            this.values.put(arg, value);
            this.argIndex++;
        }
    }

}