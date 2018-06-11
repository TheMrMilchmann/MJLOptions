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
 * An {@code Option} is a parameter that is interpreted key-based.
 *
 * <p>All options are uniquely identifiable by a case-sensitive alphanumeric key (`[A-Za-z][A-Za-z0-9]*`). These keys
 * are also referred to as "long tokens" throughout this document.</p>
 *
 * <p>Additionally it is possible to use a single alphabetic character as alternate case-sensitive key. These keys are
 * also referred as "short tokens" throughout this document.</p>
 *
 * <p>An option is "present" iff it has been discovered by the parser.</p>
 *
 *
 * <p><b>Marker Options</b><br>
 * Options may be denoted as "marker options" with an associated marker value. A marker option is treated as a regular
 * option with the following exceptions:</p>
 * <ul><li>A marker option may be explicitly present without a specified value.</li></ul>
 *
 * <p>Additionally marker options may be denoted to be usable only as markers (or "marker-only options"). A marker-only
 * option is treated as a regular option with the following exceptions:</p>
 * <ul>
 * <li>A marker-only option must either be explicitly present without a specified value or not be present at all.</li>
 * </ul>
 *
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
     * @since   0.1.0
     */
    public static <T> Builder<T> builder(String longToken, ValueParser<T> parser) {
        return new Builder<>(longToken, parser);
    }

    @Nullable
    private final Character shortToken;
    private final String longToken;

    final ValueParser<T> parser;

    @Nullable
    private final T defaultValue;
    private final boolean hasDefaultValue;

    @Nullable
    private final T markerValue;
    private final boolean hasMarkerValue;
    private final boolean isMarkerOnly;

    private Option(String longToken, @Nullable Character shortToken, ValueParser<T> parser, @Nullable T defaultValue, boolean hasDefaultValue, @Nullable T markerValue, boolean hasMarkerValue, boolean isMarkerOnly) {
        this.shortToken = shortToken;
        this.longToken = longToken;
        this.parser = parser;
        this.defaultValue = defaultValue;
        this.hasDefaultValue = hasDefaultValue;
        this.markerValue = markerValue;
        this.hasMarkerValue = hasMarkerValue;
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
     * Returns this option's default value.
     *
     * @return  this option's default value
     *
     * @throws IllegalArgumentException if this option does not have a default value
     *
     * @see #hasDefaultValue()
     *
     * @since   0.1.0
     */
    @Nullable
    public T getDefaultValue() {
        if (!this.hasDefaultValue) throw new IllegalStateException(this.toString() + " does not have a default value");
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
        return this.hasDefaultValue;
    }

    /**
     * Returns this option's marker value.
     *
     * @return  this option's marker value
     *
     * @throws IllegalArgumentException if this option does not have a marker value
     *
     * @see #hasMarkerValue()
     *
     * @since   0.1.0
     */
    @Nullable
    public T getMarkerValue() {
        if (!this.hasMarkerValue) throw new IllegalStateException(this.toString() + " does not have a marker value");
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
        return this.hasMarkerValue;
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
        sb.append(", hasDefaultValue=").append(this.hasDefaultValue);
        if (this.hasDefaultValue) sb.append(", defaultValue=").append(this.defaultValue);
        sb.append(", hasMarkerValue=").append(this.hasMarkerValue);
        if (this.hasMarkerValue) sb.append(", markerValue=").append(this.markerValue);
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
        private boolean hasDefaultValue;

        @Nullable
        private T markerValue;
        private boolean hasMarkerValue;
        private boolean isMarkerOnly;

        private Builder(String longToken, ValueParser<T> parser) {
            this.longToken = Objects.requireNonNull(longToken);
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
            return new Option<>(this.longToken, this.shortToken, this.parser, this.defaultValue, this.hasDefaultValue, this.markerValue, this.hasMarkerValue, this.isMarkerOnly);
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
         * @since   0.1.0
         */
        public Builder<T> withDefaultValue(@Nullable T value) {
            this.defaultValue = value;
            this.hasDefaultValue = true;

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
         * @since   0.1.0
         */
        public Builder<T> withMarkerValue(@Nullable T value) {
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
         * @since   0.1.0
         */
        public Builder<T> withMarkerValue(@Nullable T value, boolean isMarkerOnly) {
            this.markerValue = value;
            this.hasMarkerValue = true;
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
         * @since   0.1.0
         */
        public Builder<T> withShortToken(char shortToken) {
            this.shortToken = shortToken;

            return this;
        }

    }

}