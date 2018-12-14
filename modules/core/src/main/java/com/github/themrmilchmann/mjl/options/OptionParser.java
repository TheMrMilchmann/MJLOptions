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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import com.github.themrmilchmann.mjl.options.annotations.ArgumentHolder;
import com.github.themrmilchmann.mjl.options.annotations.DefaultValueRef;
import com.github.themrmilchmann.mjl.options.annotations.MarkerValueRef;
import com.github.themrmilchmann.mjl.options.annotations.OptionHolder;
import com.github.themrmilchmann.mjl.options.annotations.ValueParserRef;
import com.github.themrmilchmann.mjl.options.annotations.VarargHolder;
import com.github.themrmilchmann.mjl.options.annotations.WildcardHolder;
import com.github.themrmilchmann.mjl.options.internal.FieldAccess;

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
     * Constructs an {@link OptionPool} from the given class and parses parameters from an array of fragments.
     *
     * @param <T>       the type of the data object
     * @param cls       the type of the data object
     * @param lookup    the lookup which will be used to construct the data object
     * @param command   the command to be parsed
     *
     * @return  a data object holding parsed values
     *
     * @throws ClassPoolConfigurationException  if the data class is ill-formatted
     * @throws ParsingException                 if any error occurs during parsing
     *
     * @since   0.4.0
     */
    @SuppressWarnings("unchecked")
    public static <T> T parseFragments(Class<T> cls, MethodHandles.Lookup lookup, String[] command) {
        List<Throwable> errors = new ArrayList<>();

        TreeSet<ArgFieldWrapper> args = new TreeSet<>((alpha, beta) -> {
            if (alpha.index == beta.index) {
                String msg = String.format("Duplicate argument index (%s) at field '%s' and '%s'", alpha.index, alpha.field.getName(), beta.field.getName());
                errors.add(new IllegalArgumentException(msg));
            }

            return Integer.compare(alpha.index, beta.index);
        });
        List<OptFieldWrapper> opts = new ArrayList<>();
        ArgFieldWrapper varargFieldWrapper = null;
        Field wildcardField = null;

        OptionPool.Builder bPool = OptionPool.builder();
        T instance;

        try {
            MethodHandle hConstructor = lookup.findConstructor(cls, MethodType.methodType(cls));
            instance = (T) hConstructor.invokeExact();
        } catch (Throwable t) {
            throw new ParsingException("Failed to instantiate data class", t);
        }

        for (Field field : cls.getDeclaredFields()) {
            ArgumentHolder argHolder = field.getDeclaredAnnotation(ArgumentHolder.class);
            OptionHolder optHolder = field.getDeclaredAnnotation(OptionHolder.class);
            VarargHolder varargHolder = field.getDeclaredAnnotation(VarargHolder.class);
            WildcardHolder wildcardHolder = field.getDeclaredAnnotation(WildcardHolder.class);

            int i = 0;
            if (argHolder != null) i++;
            if (optHolder != null) i++;
            if (varargHolder != null) i++;
            if (wildcardHolder != null) i++;

            if (i > 1) throwAtField(field, errors, "Field must be at most one of: @ArgumentHolder, @OptionHolder, @VarargHolder, @WildcardHolder");

            DefaultValueRef defaultValueRef = field.getDeclaredAnnotation(DefaultValueRef.class);
            MarkerValueRef markerValueRef = field.getDeclaredAnnotation(MarkerValueRef.class);
            ValueParserRef valueParserRef = field.getDeclaredAnnotation(ValueParserRef.class);

            if (wildcardHolder != null) {
                if (wildcardField != null) throwAtField(field, errors, "There must be at most one @WildcardHolder");

                // Check configuration
                if (defaultValueRef != null) throwAtField(field, errors, "@WildcardHolder may not have a @DefaultValueRef");
                if (markerValueRef != null) throwAtField(field, errors, "@WildcardHolder may not have a @MarkerValueRef");
                if (valueParserRef != null) throwAtField(field, errors, "@WildcardHolder may not have a @ValueParserRef");

                wildcardField = field;
            } else {
                ValueParser<?> valueParser = null;

                if (valueParserRef != null) {
                    String ref = valueParserRef.value();
                    valueParser = (ValueParser<?>) lookupRef(ref, lookup);
                }

                if (valueParser == null) {
                    Class<?> type = field.getType();

                    if (type == boolean.class) {
                        valueParser = ValueParser.BOOLEAN;
                    } else if (type == byte.class) {
                        valueParser = ValueParser.BYTE;
                    } else if (type == char.class) {
                        valueParser = ValueParser.CHARACTER;
                    } else if (type == short.class) {
                        valueParser = ValueParser.SHORT;
                    } else if (type == int.class) {
                        valueParser = ValueParser.INT;
                    } else if (type == long.class) {
                        valueParser = ValueParser.LONG;
                    } else if (type == float.class) {
                        valueParser = ValueParser.FLOAT;
                    } else if (type == double.class) {
                        valueParser = ValueParser.DOUBLE;
                    } else {
                        throwAtField(field, errors, "Failed to infer ValueParser for field");
                        continue;
                    }
                }

                if (argHolder != null) {
                    // Check configuration
                    if (markerValueRef != null) throwAtField(field, errors, "@WildcardHolder may not have a @MarkerValueRef");

                    Argument.Builder<?> bArg = Argument.builder(valueParser, argHolder.optional());

                    if (defaultValueRef != null) {
                        String ref = defaultValueRef.value();
                        Object value = lookupRef(ref, lookup);
                        bArg.withDefaultValueInternal(value);
                    }

                    args.add(new ArgFieldWrapper(field, bArg.build(), argHolder.index()));
                } else if (optHolder != null) {
                    Option.Builder<?> bOpt = Option.builder(optHolder.longToken(), valueParser);
                    if (optHolder.shortToken() != '\0') bOpt.withShortToken(optHolder.shortToken());

                    if (markerValueRef != null) {
                        String ref = markerValueRef.value();
                        Object value = lookupRef(ref, lookup);
                        bOpt.withMarkerValueInternal(value, markerValueRef.markerOnly());
                    }

                    if (defaultValueRef != null) {
                        String ref = defaultValueRef.value();
                        Object value = lookupRef(ref, lookup);
                        bOpt.withDefaultValueInternal(value);
                    }

                    Option<?> opt = bOpt.build();
                    bPool.withOption(opt);
                    opts.add(new OptFieldWrapper(field, opt));
                } else if (varargHolder != null) {
                    if (varargFieldWrapper != null) throwAtField(field, errors, "There must be at most one @VarargHolder");

                    Argument<?> arg = Argument.builder(valueParser, varargHolder.optional()).build();
                    varargFieldWrapper = new ArgFieldWrapper(field, arg, Integer.MAX_VALUE);
                }
            }
        }

        if (!errors.isEmpty()) throw new ClassPoolConfigurationException(errors);

        for (ArgFieldWrapper wrapper : args) bPool.withArg(wrapper.arg);

        if (varargFieldWrapper != null) {
            bPool.withVarargArg(varargFieldWrapper.arg);
            args.add(varargFieldWrapper);
        }

        OptionSet set = parseFragments(bPool.build(), command);

        for (ArgFieldWrapper wrapper : args) {
            if (set.isSet(wrapper.arg)) {
                FieldAccess.set(wrapper.field, instance, set.get(wrapper.arg), lookup);
            } else if (!wrapper.arg.isOptional()) {
                throw new ParsingException("Required argument has not been specified");
            }
        }

        for (OptFieldWrapper wrapper : opts) {
            if (set.isSet(wrapper.opt)) {
                FieldAccess.set(wrapper.field, instance, set.get(wrapper.opt), lookup);
            }
        }

        return instance;
    }

    @Nullable
    private static Object lookupRef(String ref, MethodHandles.Lookup lookup) {
        Object value;

        if (ref.contains("::")) {
            String[] s = ref.split("::");
            Class<?> cntCls;

            try {
                cntCls = Class.forName(s[0]);
            } catch (ClassNotFoundException e) {
                throw new ParsingException("Failed to resolve reference", e);
            }

            if (s[1].endsWith("()")) {
                s[1] = s[1].substring(0, s[1].length() - 3);

                try {
                    Method method = cntCls.getDeclaredMethod(s[1]);
                    MethodHandle hMethod = lookup.unreflect(method);
                    value = hMethod.invokeExact();
                } catch (Throwable t) {
                    throw new ParsingException("Failed to resolve referenced method", t);
                }
            } else {
                try {
                    Field vpField = cntCls.getDeclaredField(s[1]);
                    value = FieldAccess.getStatic(vpField, lookup);
                } catch (Exception e) {
                    throw new ParsingException("Failed to resolve referenced field", e);
                }
            }
        } else {
            try {
                Class<?> vpCls = Class.forName(ref);
                MethodHandle hVPCtr = lookup.findConstructor(vpCls, MethodType.methodType(vpCls));
                value = (ValueParser<?>) hVPCtr.invokeExact();
            } catch (Throwable t) {
                throw new ParsingException("Failed to resolve reference", t);
            }
        }

        return value;
    }

    private static void throwAtField(Field field, List<Throwable> errors, String msg) {
        errors.add(new IllegalArgumentException(String.format("Field %s: %s", field.getName(), msg)));
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
     * {@link #lineToFragments(String) Converts the given line to fragments} and
     * {@link #parseFragments(Class, MethodHandles.Lookup, String[])}  parses} those.
     *
     * @param <T>       the type of the data object
     * @param cls       the type of the data object
     * @param lookup    the lookup which will be used to construct the data object
     * @param line      the line to be parsed
     *
     * @return  a data object holding parsed values
     *
     * @since   0.4.0
     */
    public static <T> T parseLine(Class<T> cls, MethodHandles.Lookup lookup, String line) {
        return parseFragments(cls, lookup, lineToFragments(line));
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
        // https://github.com/Project-Skara/jdk/blob/c2105ced865fba11fbf8d4a8e18a59fcb1fe10fd/src/java.base/windows/native/libjli/cmdtoargs.c#L203
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

    private static final class ArgFieldWrapper {

        private final Field field;
        private final Argument<?> arg;
        private final int index;

        private ArgFieldWrapper(Field field, Argument<?> arg, int index) {
            this.field = field;
            this.arg = arg;
            this.index = index;
        }

    }

    private static final class OptFieldWrapper {

        private final Field field;
        private final Option<?> opt;

        private OptFieldWrapper(Field field, Option<?> opt) {
            this.field = field;
            this.opt = opt;
        }

    }

}