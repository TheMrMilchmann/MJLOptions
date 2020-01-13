/*
 * Copyright 2018-2020 Leon Linhart
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
 * Options are key-based interpreted parameters.
 *
 * <p>All options are uniquely identifiable by a case-sensitive key. These keys are also referred to as <i>long
 * tokens</i> throughout this document. The key must match the pattern: {@code [A-Za-z]([A-Za-z0-9]|-|\.)*}</p>
 *
 * <p>Additionally it is possible to use a single alphabetic character as alternate case-sensitive key. These keys are
 * also referred as <i>short tokens</i> throughout this document.</p>
 *
 * <p>By default, a option are known to the parser ahead of time and require a value to be specified. Options that use
 * this default behavior are also referred to as <i>regular options</i> when necessary.</p>
 *
 * <h3>Marker options</h3>
 * <p>Options may be denoted as <i>marker options</i>.</p>
 * <ul><li>A marker option allows for (but does not require) a value to be specified.</li></ul>
 *
 * <h3>Marker-only options</h3>
 * <p>Options may be denoted as <i>marker-only options</i>.</p>
 * <ul><li>A Marker-only option does not allow for a value to be specified.</li></ul>
 *
 * @param <T> the type of the options value
 *
 * @see OptionParser
 * @see Argument
 *
 * @since   0.1.0
 *
 * @author  Leon Linhart
 */
public final class Option<T> {

    /**
     * Returns a builder instance for options with the given long token and {@link ValueParser parser}.
     *
     * @param <T>       the type of the options value
     * @param longToken the long token for the option
     * @param parser    the parser for the option
     *
     * @return  a builder instance
     *
     * @throws IllegalArgumentException if the long option token is invalid
     * @throws NullPointerException     if {@code null} is passed to any of the parameters
     *
     * @since   0.1.0
     */
    public static <T> Builder<T> builder(String longToken, ValueParser<T> parser) {
        return new Builder<>(longToken, parser);
    }

    /**
     * Convenience shortcut for {@code Option.builder(...).build()}.
     *
     * @param <T>       the type of the options value
     * @param longToken the long token for the option
     * @param parser    the parser for the option
     *
     * @return  an option with the given properties
     *
     * @throws IllegalArgumentException if the long option token is invalid
     * @throws NullPointerException     if {@code null} is passed to any of the parameters
     *
     * @since   0.4.0
     */
    public static <T> Option<T> build(String longToken, ValueParser<T> parser) {
        return new Builder<>(longToken, parser).build();
    }

    @Nullable
    private final Character shortToken;
    private final String longToken;

    final ValueParser<T> parser;

    @Nullable
    private final T defaultValue;

    @Nullable
    private final T markerValue;
    private final boolean isMarkerOnly;

    private Option(String longToken, @Nullable Character shortToken, ValueParser<T> parser, @Nullable T defaultValue, @Nullable T markerValue, boolean isMarkerOnly) {
        this.shortToken = shortToken;
        this.longToken = longToken;
        this.parser = parser;
        this.defaultValue = defaultValue;
        this.markerValue = markerValue;
        this.isMarkerOnly = isMarkerOnly;
    }

    /**
     * Returns this option's long token.
     *
     * @return  this option's long token
     *
     * @since   0.1.0
     */
    public String getLongToken() {
        return this.longToken;
    }

    /**
     * Returns this option's short token, or {@code null} if this option has no short token.
     *
     * @return  this option's short token, or {@code null}
     *
     * @since   0.1.0
     */
    @Nullable
    public Character getShortToken() {
        return this.shortToken;
    }

    /**
     * Returns this option's default value, or {@code null}.
     *
     * @return  this option's default value, or {@code null}
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
     * Returns whether or not this option has a default value.
     *
     * @return  {@code true} if this option has a default value, or {@code false} otherwise
     *
     * @see #getDefaultValue()
     *
     * @since   0.1.0
     */
    public boolean hasDefaultValue() {
        return this.defaultValue != null;
    }

    /**
     * Returns this option's marker value, or {@code null}.
     *
     * @return  this option's marker value, or {@code null}
     *
     * @see #hasMarkerValue()
     *
     * @since   0.1.0
     */
    @Nullable
    public T getMarkerValue() {
        return this.markerValue;
    }

    /**
     * Returns whether or not this option has a marker value.
     *
     * @return {@code true} if this option has a marker value, or {@code false} otherwise
     *
     * @see #getMarkerValue()
     *
     * @since   0.1.0
     */
    public boolean hasMarkerValue() {
        return this.markerValue != null;
    }

    /**
     * Returns whether or not this option must be used as a marker option.
     *
     * @return  {@code true} if this option must be used as a marker, or {@code false} otherwise
     *
     * @see #getMarkerValue()
     *
     * @since   0.1.0
     */
    public boolean isMarkerOnly() {
        return this.isMarkerOnly;
    }

    /**
     * {@inheritDoc}
     *
     * @since   0.1.0
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Option[");
        sb.append("long=").append(this.longToken);
        if (this.shortToken != null) sb.append(", short=").append(this.shortToken);
        if (this.defaultValue != null) sb.append(", defaultValue=").append(this.defaultValue);
        if (this.markerValue != null) sb.append(", markerValue=").append(this.markerValue);
        sb.append(", markerOnly=").append(this.isMarkerOnly);
        sb.append("]");

        return sb.toString();
    }

    /**
     * A builder for an {@link Option}.
     *
     * @param <T> the type for the option's value
     *
     * @since   0.1.0
     */
    public static final class Builder<T> {

        private final String longToken;
        private final ValueParser<T> parser;

        @Nullable
        private Character shortToken;

        @Nullable
        private T defaultValue;

        @Nullable
        private T markerValue;
        private boolean isMarkerOnly;

        private Builder(String longToken, ValueParser<T> parser) {
            this.longToken = Objects.requireNonNull(longToken);
            if (!longToken.matches(OptionParser.REGEX_LONG_TOKEN)) throw new IllegalArgumentException("Invalid long option token.");

            this.parser = Objects.requireNonNull(parser);
        }

        /**
         * Returns a new immutable {@linkplain Option}.
         *
         * @return a new immutable option
         *
         * @since   0.1.0
         */
        public Option<T> build() {
            return new Option<>(this.longToken, this.shortToken, this.parser, this.defaultValue, this.markerValue, this.isMarkerOnly);
        }

        /**
         * Sets the default value for the option.
         *
         * <p>Overrides any previously set default value.</p>
         *
         * @param value the default value for the option
         *
         * @return  this builder instance
         *
         * @throws NullPointerException if the given value is {@code null}
         *
         * @since   0.1.0
         */
        public Builder<T> withDefaultValue(T value) {
            this.defaultValue = Objects.requireNonNull(value, "The value of an option may not be null.");
            return this;
        }

        @SuppressWarnings("unchecked")
        Builder<T> withDefaultValueInternal(Object value) {
            this.defaultValue = (T) Objects.requireNonNull(value, "The value of an option may not be null.");
            return this;
        }

        /**
         * Sets the marker value for the option.
         *
         * <p>Overrides any previously set marker value.</p>
         *
         * <p>This is an utility method. Using it is equivalent to calling
         * {@code builder.withMarkerValue(value, false)}.</p>
         *
         * @param value the marker value for the option.
         *
         * @return  this builder instance
         *
         * @throws NullPointerException if the given value is {@code null}
         *
         * @since   0.1.0
         */
        public Builder<T> withMarkerValue(T value) {
            return this.withMarkerValue(value, false);
        }

        /**
         * Sets the marker value for the option.
         *
         * <p>Overrides any previously set marker value.</p>
         *
         * @param value         the marker value for the option
         * @param isMarkerOnly  whether or not the option must be used as a marker
         *
         * @return  this builder instance
         *
         * @throws NullPointerException if the given value is {@code null}
         *
         * @since   0.1.0
         */
        public Builder<T> withMarkerValue(T value, boolean isMarkerOnly) {
            this.markerValue = Objects.requireNonNull(value, "The value of an option may not be null.");
            this.isMarkerOnly = isMarkerOnly;

            return this;
        }

        @SuppressWarnings("unchecked")
        Builder<T> withMarkerValueInternal(Object value, boolean isMarkerOnly) {
            this.markerValue = (T) Objects.requireNonNull(value, "The value of an option may not be null.");
            this.isMarkerOnly = isMarkerOnly;

            return this;
        }

        /**
         * Sets the short token for the option.
         *
         * @param shortToken the short token for the option
         *
         * @return  this builder instance
         *
         * @throws IllegalArgumentException if the given token is invalid
         *
         * @since   0.1.0
         */
        public Builder<T> withShortToken(char shortToken) {
            if (!Character.isAlphabetic(shortToken)) throw new IllegalArgumentException("Short option tokens must be alphabetic.");

            this.shortToken = shortToken;

            return this;
        }

    }

}