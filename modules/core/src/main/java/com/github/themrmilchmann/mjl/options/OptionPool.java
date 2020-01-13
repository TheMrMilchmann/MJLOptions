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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import com.github.themrmilchmann.mjl.options.internal.KNFFormula;

/**
 * An {@code OptionPool} is a collection of available {@link Argument arguments} and {@link Option options}.
 *
 * @since   0.1.0
 *
 * @author  Leon Linhart
 */
public final class OptionPool {

    /**
     * Returns a builder for an {@code OptionPool}.
     *
     * @return  a builder instance
     *
     * @since   0.1.0
     */
    public static Builder builder() {
        return new OptionPool.Builder();
    }

    private final Argument[] args;
    private final boolean isLastVararg;
    private final Map<Character, Option<?>> optShortTokens;
    private final Map<String, Option<?>> optLongTokens;
    final Set<Restriction> restrictions;

    private OptionPool(Argument[] args, boolean isLastVararg, Map<Character, Option<?>> sTokens, Map<String, Option<?>> lTokens, Set<Restriction> restrictions) {
        this.args = args;
        this.isLastVararg = isLastVararg;
        this.optShortTokens = sTokens;
        this.optLongTokens = lTokens;
        this.restrictions = restrictions;
    }

    /**
     * Returns whether or not the given {@link Argument argument} is in this pool.
     *
     * @param arg   argument whose presence in this pool is to be tested
     *
     * @return  whether or not the given argument is in this pool
     *
     * @throws NullPointerException if the given argument is {@code null}
     *
     * @since   0.1.0
     */
    public boolean contains(Argument<?> arg) {
        Objects.requireNonNull(arg);

        for (Argument e : this.args) {
            if (e == arg) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns whether or not the given {@link Option option} is in this pool.
     *
     * @param option    option whose presence in this pool is to be tested
     *
     * @return  whether or not the given option is in this pool
     *
     * @throws NullPointerException if the given option is {@code null}
     *
     * @since   0.1.0
     */
    public boolean contains(Option<?> option) {
        Objects.requireNonNull(option);
        return this.optLongTokens.containsValue(option);
    }

    /**
     * Returns the argument at the given position.
     *
     * @param index the index to lookup
     *
     * @return  the argument at the given position
     *
     * @throws IndexOutOfBoundsException    if the given {@code index} is less than zero or greater than or equal to the
     *                                      {@link #getArgumentCount() number of arguments} in this pool
     *
     * @since   0.1.0
     */
    public Argument<?> getArgument(int index) {
        if (index < 0 || index >= this.args.length) throw new IndexOutOfBoundsException();
        return this.args[index];
    }

    Argument<?> getLastArgument() {
        // In case this method is ever made public validation MUST be added here.
        return this.args[this.args.length - 1];
    }

    /**
     * Returns the option for the given token, or {@code null}.
     *
     * @param token the short token
     *
     * @return  the option for the given token in this pool
     *
     * @since   0.1.0
     */
    @Nullable
    public Option<?> getOption(char token) {
        return this.optShortTokens.get(token);
    }

    /**
     * Returns the option for the given token, or {@code null}.
     *
     * @param token the long token
     *
     * @return  the option for the given token in this pool
     *
     * @since   0.1.0
     */
    @Nullable
    public Option<?> getOption(String token) {
        return this.optLongTokens.get(token);
    }

    /**
     * Returns the number of arguments that are in this pool.
     *
     * @return  the number of arguments in this pool
     *
     * @since   0.1.0
     */
    public int getArgumentCount() {
        return this.args.length;
    }

    /**
     * Returns whether or not an argument with the given index exists in this pool.
     *
     * @param index the index to check
     *
     * @return  whether or not an argument with the given index exists in this pool
     *
     * @since   0.1.0
     */
    public boolean hasArgument(int index) {
        return index >= 0 && index < this.args.length;
    }

    /**
     * Returns whether or not this pool contains any arguments.
     *
     * @return  whether or not this pool contains arguments
     *
     * @since   0.1.0
     */
    public boolean hasArguments() {
        return this.args.length > 0;
    }

    /**
     * Returns the index for the given {@link Argument} if it is present in this pool, or {@code -1} otherwise.
     *
     * @param arg   argument to search for
     *
     * @return  the index for the given {@link Argument} if it is present in this pool, or {@code -1} otherwise
     *
     * @throws NullPointerException if the given argument is {@code null}
     *
     * @since   0.1.0
     */
    public int indexOf(Argument<?> arg) {
        Objects.requireNonNull(arg);

        for (int i = 0; i < this.args.length; i++) {
            if (this.args[i] == arg) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Returns whether or not the last argument is a vararg argument.
     *
     * @return  whether or not the last argument is a vararg argument
     *
     * @since   0.1.0
     */
    public boolean isLastVararg() {
        return this.isLastVararg;
    }

    /**
     * A builder for an {@link OptionPool}.
     *
     * @since   0.1.0
     */
    public static final class Builder {

        private final List<Argument<?>> args = new ArrayList<>();
        private final Map<Character, Option<?>> sTokens = new HashMap<>();
        private final Map<String, Option<?>> lTokens = new HashMap<>();
        private final Set<Restriction> restrictions = new HashSet<>();
        private boolean isLastVararg;

        private Builder() {}

        /**
         * Returns a new immutable {@linkplain OptionPool}.
         *
         * <p>This method performs extensive reachability checks and throws if one or more options are unreachable due
         * to the restrictions in place.</p>
         *
         * @return  a new immutable pool
         *
         * @throws UnreachableOptionException   if one or more options are unreachable to due the restrictions in place
         *
         * @since   0.1.0
         */
        public OptionPool build() {
            KNFFormula.Builder<Option<?>> formula = KNFFormula.builder(this.lTokens.values());
            this.restrictions.stream().map(Restriction::getClauses).flatMap(Set::stream).forEach(formula::and);

            Set<Option<?>> unreachable = formula.calculateUnreachableOptions();
            if (!unreachable.isEmpty()) throw new UnreachableOptionException(unreachable);

            return new OptionPool(this.args.toArray(new Argument[0]), this.isLastVararg, this.sTokens, this.lTokens, this.restrictions);
        }

        /**
         * Adds an argument for the option pool.
         *
         * @param arg   the argument to add to this pool
         *
         * @return  this builder instance
         *
         * @since   0.1.0
         */
        public Builder withArg(Argument<?> arg) {
            if (this.isLastVararg) throw new IllegalStateException();
            if (!this.args.isEmpty() && this.args.get(this.args.size() - 1).isOptional()) throw new IllegalStateException();

            this.args.add(arg);
            return this;
        }

        /**
         * Adds a vararg argument for the option pool.
         *
         * @param arg   the argument to add to this pool
         *
         * @return  this builder instance
         *
         * @since   0.1.0
         */
        public Builder withVarargArg(Argument<?> arg) {
            this.withArg(arg);
            this.isLastVararg = true;

            return this;
        }

        /**
         * Adds an option for the option pool.
         *
         * @param opt   the option to add to this pool
         *
         * @return  this builder instance
         *
         * @since   0.1.0
         */
        public Builder withOption(Option<?> opt) {
            this.lTokens.put(opt.getLongToken(), opt);
            if (opt.getShortToken() != null) this.sTokens.put(opt.getShortToken(), opt);

            return this;
        }

        /**
         * Adds a {@link Restriction} for the option pool.
         *
         * @param restriction   the restriction to add to this pool
         *
         * @return  this builder instance
         *
         * @since   0.4.0
         */
        public Builder withRestriction(Restriction restriction) {
            if (!this.lTokens.values().containsAll(restriction.getOptions())) throw new IllegalArgumentException();
            this.restrictions.add(restriction);

            return this;
        }

    }

}