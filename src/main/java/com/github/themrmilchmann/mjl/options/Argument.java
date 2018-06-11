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

import java.util.Objects;
import javax.annotation.Nullable;

/**
 * An {@code Argument} is a parameter that is interpreted index-based.
 *
 * <p>An argument is "present" iff it has been discovered by the parser.</p>
 *
 * <p><b>Optional Arguments</b><br>
 * Arguments may be denoted as "optional arguments" (optionally) with an associated default value. An optional argument
 * is treated as a regular argument with the following exception:</p>
 * <ul>
 * <li>An optional argument may not be present. (In this case if the argument has a default value, it is assigned as
 * value for the argument.)</li>
 * </ul>
 *
 * <p>Any argument with an index greater than an optional argument must also be optional.</p>
 *
 * <p><b>Vararg Argument</b><br>
 * The trailing argument may be denoted as "vararg argument".<br>
 * An indefinite amount of values may be assigned to a vararg argument. Vararg arguments may additionally be optional.
 * If a vararg argument is not optional, at least one value must be explicitly assigned.</p>
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
     * @since   0.1.0
     */
    public static <T> Builder<T> builder(ValueParser<T> parser) {
        return builder(parser, false);
    }

    /**
     * Returns a builder instance for arguments with the given {@link ValueParser parser}.
     *
     * @param <T>           the type of the arguments value
     * @param parser        the parser for the argument
     * @param isOptional    whether or not the argument is optional
     *
     * @return  a builder instance
     *
     * @since   0.1.0
     */
    public static <T> Builder<T> builder(ValueParser<T> parser, boolean isOptional) {
        return new Builder<>(parser, isOptional);
    }

    final ValueParser<T> parser;
    private final boolean isOptional;

    @Nullable
    private final T defaultValue;
    private final boolean hasDefaultValue;

    private Argument(ValueParser<T> parser, boolean isOptional, @Nullable T defaultValue, boolean hasDefaultValue) {
        this.parser = parser;
        this.isOptional = isOptional;
        this.defaultValue = defaultValue;
        this.hasDefaultValue = hasDefaultValue;
    }

    /**
     * Returns the default value for this argument.
     *
     * @return  the default value for this argument
     *
     * @throws IllegalStateException    if this argument does not have a default value
     *
     * @see #hasDefaultValue()
     *
     * @since   0.1.0
     */
    @Nullable
    public T getDefaultValue() {
        if (!this.hasDefaultValue) throw new IllegalStateException("Argument does not have a default value: " + this.toString());
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
        return this.hasDefaultValue;
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
        sb.append(", hasDefaultValue=").append(this.hasDefaultValue);
        if (this.hasDefaultValue) sb.append(", defaultValue=").append(this.defaultValue);
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
        private final boolean isOptional;

        @Nullable
        private T defaultValue;
        private boolean hasDefault;

        private Builder(ValueParser<T> parser, boolean isOptional) {
            this.parser = Objects.requireNonNull(parser);
            this.isOptional = isOptional;
        }

        /**
         * Returns a new immutable {@linkplain Argument}.
         *
         * @return  a new immutable argument
         *
         * @since   0.1.0
         */
        public Argument<T> build() {
            return new Argument<>(this.parser, this.isOptional, this.defaultValue, this.hasDefault);
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
        public Builder<T> withDefaultValue(@Nullable T value) {
            this.defaultValue = value;
            this.hasDefault = true;

            return this;
        }

    }

}