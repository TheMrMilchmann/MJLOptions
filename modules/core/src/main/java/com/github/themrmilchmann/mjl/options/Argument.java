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

import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Arguments are index-based interpreted parameters.
 *
 * <h3>Optional arguments</h3>
 * <p>Arguments may be denoted as <i>optional arguments</i>.</p>
 * <ul>
 *     <li>Optional arguments do not require a value to be specified.</li>
 *     <li>All arguments required after an optional argument must also be optional.</li>
 * </ul>
 *
 * <h3>Vararg arguments</h3>
 * <p>The trailing argument may be denoted as <i>vararg argument</i>.</p>
 * <ul>
 *     <li>An indefinite amount of values may be assigned to a vararg argument.</li>
 *     <li>Vararg arguments may additionally be optional. (If a vararg argument is not optional, at least one value must
 *     be explicitly assigned.)</li>
 * </ul>
 *
 * @param <T>   the type of the arguments value
 *
 * @since   0.1.0
 *
 * @see OptionParser
 * @see OptionPool
 *
 * @author  Leon Linhart
 */
public final class Argument<T> {

    /**
     * Returns a builder instance for arguments with the given {@link ValueParser parser}.
     *
     * @param <T>       the type of the arguments value
     * @param parser    the parser for the argument
     *
     * @return  a builder instance
     *
     * @throws NullPointerException if {@code null} is passed to any of the parameters
     *
     * @since   0.1.0
     */
    public static <T> Builder<T> builder(ValueParser<T> parser) {
        return new Builder<>(parser);
    }

    /**
     * Convenience shortcut for {@code Argument.builder(...).build()}.
     *
     * @param <T>       the type of the arguments value
     * @param parser    the parser for the argument
     *
     * @return  an argument of the given type
     *
     * @throws NullPointerException     if {@code null} is passed to any of the parameters
     *
     * @since   0.4.0
     */
    public static <T> Argument<T> build(ValueParser<T> parser) {
        return new Builder<>(parser).build();
    }

    final ValueParser<T> parser;
    private final boolean isOptional;

    @Nullable
    private final T defaultValue;

    private Argument(ValueParser<T> parser, boolean isOptional, @Nullable T defaultValue) {
        this.parser = parser;
        this.isOptional = isOptional;
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the default value for this argument, or {@code null}.
     *
     * @return  the default value for this argument, or {@code null}
     *
     * @see #hasDefaultValue()
     *
     * @since   0.1.0
     */
    @Nullable
    public T getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Returns whether or not this argument has a default value.
     *
     * @return  {@code true} if this argument has a default value, or {@code false} otherwise
     *
     * @see #getDefaultValue()
     *
     * @since   0.1.0
     */
    public boolean hasDefaultValue() {
        return this.defaultValue != null;
    }

    /**
     * Returns whether or not this argument is optional.
     *
     * @return  {@code true} if this argument is optional, or {@code false} otherwise
     *
     * @since   0.1.0
     */
    public boolean isOptional() {
        return this.isOptional;
    }

    /**
     * {@inheritDoc}
     *
     * @since   0.1.0
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Argument[");
        sb.append("isOptional=").append(this.isOptional);
        if (this.defaultValue != null) sb.append(", defaultValue=").append(this.defaultValue);
        sb.append("]");

        return sb.toString();
    }

    /**
     * A builder for an {@link Argument}.
     *
     * @param <T> the type for the arguments value
     *
     * @since   0.1.0
     */
    public static final class Builder<T> {

        private final ValueParser<T> parser;
        private boolean isOptional;

        @Nullable
        private T defaultValue;

        private Builder(ValueParser<T> parser) {
            this.parser = Objects.requireNonNull(parser);
            this.isOptional = false;
        }

        /**
         * Returns a new immutable {@linkplain Argument}.
         *
         * @return  a new immutable argument
         *
         * @since   0.1.0
         */
        public Argument<T> build() {
            return new Argument<>(this.parser, this.isOptional, this.defaultValue);
        }

        /**
         * Sets whether or not the argument will be optional.
         *
         * <p>Overrides any previously set default value.</p>
         *
         * @param value whether or not the argument will be optional
         *
         * @return  this builder instance
         *
         * @since   0.4.0
         */
        public Builder<T> optional(boolean value) {
            this.isOptional = value;
            return this;
        }

        /**
         * Sets the default value for the argument.
         *
         * <p>Overrides any previously set default value.</p>
         *
         * @param value the default value for the argument
         *
         * @return  this builder instance
         *
         * @since   0.1.0
         */
        public Builder<T> withDefaultValue(T value) {
            this.defaultValue = Objects.requireNonNull(value);
            return this;
        }

        @SuppressWarnings("unchecked")
        Argument.Builder<T> withDefaultValueInternal(Object value) {
            this.defaultValue = (T) Objects.requireNonNull(value);
            return this;
        }

    }

}