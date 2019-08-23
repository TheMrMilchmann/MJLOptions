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
package com.github.themrmilchmann.mjl.options;

/**
 * A {@code ValueParser} provides a conversion method to convert a {@linkplain String} to a value.
 *
 * <p>This class contains predefined parsers for frequently used primitives.</p>
 *
 * @param <T>   the type of the parsed value
 *
 * @since   0.1.0
 *
 * @author  Leon Linhart
 */
@FunctionalInterface
public interface ValueParser<T> {

    /**
     * A simple parser for parsing {@code Boolean} values.
     *
     * @since   0.1.0
     */
    ValueParser<Boolean> BOOLEAN = (it) -> it.equals("1") || it.equalsIgnoreCase("true");

    /**
     * A simple parser for parsing {@code Byte} values.
     *
     * <p>This parser delegates to {@link Byte#valueOf(String)}.</p>
     *
     * @since   0.1.0
     */
    @SuppressWarnings("NullableProblems")
    ValueParser<Byte> BYTE = Byte::valueOf;

    /**
     * A simple parser for parsing {@code Character} values.
     *
     * @since   0.4.0
     */
    ValueParser<Character> CHARACTER = it -> {
        if (it.length() != 1) throw new ParsingException("Character values may only contain a single char.", new IllegalArgumentException());
        return it.charAt(0);
    };

    /**
     * A simple parser for parsing {@code Short} values.
     *
     * <p>This parser delegates to {@link Short#valueOf(String)}.</p>
     *
     * @since   0.1.0
     */
    ValueParser<Short> SHORT = Short::valueOf;

    /**
     * A simple parser for parsing {@code Integer} values.
     *
     * <p>This parser delegates to {@link Integer#valueOf(String)}.</p>
     *
     * @since   0.1.0
     */
    ValueParser<Integer> INT = Integer::valueOf;

    /**
     * A simple parser for parsing {@code Long} values.
     *
     * <p>This parser delegates to {@link Long#valueOf(String)}.</p>
     *
     * @since   0.1.0
     */
    ValueParser<Long> LONG = Long::valueOf;

    /**
     * A simple parser for parsing {@code Float} values.
     *
     * <p>This parser delegates to {@link Float#valueOf(String)}.</p>
     *
     * @since   0.1.0
     */
    ValueParser<Float> FLOAT = Float::valueOf;

    /**
     * A simple parser for parsing {@code Double} values.
     *
     * <p>This parser delegates to {@link Double#valueOf(String)}.</p>
     *
     * @since   0.1.0
     */
    ValueParser<Double> DOUBLE = Double::valueOf;

    /**
     * A simple parser for parsing {@code String} values.
     *
     * <p>This parser delegates to {@link Double#valueOf(String)}.</p>
     *
     * @since   0.1.0
     */
    @SuppressWarnings("NullableProblems")
    ValueParser<String> STRING = String::valueOf;

    /**
     * Parse a value from a given {@linkplain String}.
     *
     * @param string the {@code String} to be parsed
     *
     * @return  the parsed value (must not be {@code null})
     *
     * @throws ParsingException if an error occurs while parsing
     *
     * @since   0.1.0
     */
    T parse(String string);

}