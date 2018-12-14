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

import javax.annotation.Nullable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * An {@code OptionSet} represents a collection of arguments and options associated with their parsed values.
 *
 * @since   0.1.0
 *
 * @author  Leon Linhart
 */
public final class OptionSet {

    private final OptionPool pool;
    private final Map<Object, Object> values;
    private final Map<String, String> dynamics;

    OptionSet(OptionPool pool, Map<Object, Object> values, Map<String, String> dynamics) {
        this.pool = pool;
        this.values = values;
        this.dynamics = dynamics;
    }

    /**
     * The {@link OptionPool pool} of available arguments and options for this set.
     *
     * @return  the pool of available arguments and options for this set
     *
     * @since   0.1.0
     */
    public OptionPool getPool() {
        return this.pool;
    }

    /**
     * Returns the value for the given {@link Argument argument}.
     *
     * <ol>
     *     <li>Returns the explicitly set value for the given argument. (if available)</li>
     *     <li>Returns the default value for the given argument. (if available)</li>
     * </ol>
     *
     * @param <T>   the type of the argument's value
     * @param arg   the argument to retrieve the value for
     *
     * @return  the value for the given argument
     *
     * @throws NullPointerException     if the given argument is {@code null}
     * @throws IllegalArgumentException if the given argument is <em>not</em> in the pool that this set was created from
     * @throws NoSuchElementException   if no value has been set explicitly for the given argument and the argument does
     *                                  <em>not</em> have a default value
     *
     * @since   0.1.0
     */
    @SuppressWarnings({"unchecked"})
    @Nullable
    public <T> T get(Argument<T> arg) {
        if (!this.pool.contains(Objects.requireNonNull(arg))) throw new IllegalArgumentException();

        T res;

        if (this.values.containsKey(arg)) {
            res = (T) this.values.get(arg);
        } else {
            if (!arg.hasDefaultValue()) throw new NoSuchElementException();
            res = arg.getDefaultValue();
        }

        return res;
    }

    /**
     * Returns the value for the given {@link Option option}.
     *
     * <ol>
     *     <li>Returns the explicitly set value for the given option. (if available)</li>
     *     <li>Returns the default value for the given option. (if available)</li>
     * </ol>
     *
     * @param <T>   the type of the option's value
     * @param opt   the option to retrieve the value for
     *
     * @return  the value for the given option
     *
     * @throws NullPointerException     if the given option is {@code null}
     * @throws IllegalArgumentException if the given option is <em>not</em> in the pool that this set was created from
     * @throws NoSuchElementException   if no value has been set explicitly for the given option and the option does
     *                                  <em>not</em> have a default value
     *
     * @since   0.1.0
     */
    @SuppressWarnings({"unchecked"})
    @Nullable
    public <T> T get(Option<T> opt) {
        if (!this.pool.contains(Objects.requireNonNull(opt))) throw new IllegalArgumentException();

        T res;

        if (this.values.containsKey(opt)) {
            res = (T) this.values.get(opt);
        } else {
            if (!opt.hasDefaultValue()) throw new NoSuchElementException();
            res = opt.getDefaultValue();
        }

        return res;
    }

    /**
     * Returns the value for the given {@link Argument argument}.
     *
     * <ol>
     *     <li>Returns the explicitly set value for the given argument. (if available)</li>
     *     <li>Returns the default value for the given argument. (if available)</li>
     *     <li>Returns the given {@code other} value</li>
     * </ol>
     *
     * @param <T>   the type of the argument's value
     * @param arg   the argument to retrieve the value for
     * @param other the alternate value
     *
     * @return the value for the given argument
     *
     * @throws NullPointerException     if the given argument is {@code null}
     * @throws IllegalArgumentException if the given argument is <em>not</em> in the pool that this set was created from
     *
     * @since   0.1.0
     */
    @SuppressWarnings({"unchecked"})
    @Nullable
    public <T> T getOrElse(Argument<T> arg, @Nullable T other) {
        if (this.pool.contains(Objects.requireNonNull(arg))) throw new IllegalArgumentException();

        T res;

        if (this.values.containsKey(arg)) {
            res = (T) this.values.get(arg);
        } else if (arg.hasDefaultValue()) {
            res = arg.getDefaultValue();
        } else {
            res = other;
        }

        return res;
    }

    /**
     * Returns the value for the given {@link Argument argument}.
     *
     * <ol>
     *     <li>Returns the explicitly set value for the given argument. (if available)</li>
     *     <li>Returns the default value for the given argument. (if available)</li>
     *     <li>Runs the given {@code factory} and returns its value</li>
     * </ol>
     *
     * @param <T>       the type of the argument's value
     * @param arg       the argument to retrieve the value for
     * @param factory   the factory to compute otherwise
     *
     * @return  the value for the given argument
     *
     * @throws NullPointerException     if the given argument or factory is {@code null}
     * @throws IllegalArgumentException if the given argument is <em>not</em> in the pool that this set was created from
     *
     * @since   0.3.0
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getOrElse(Argument<T> arg, Supplier<T> factory) {
        if (this.pool.contains(Objects.requireNonNull(arg))) throw new IllegalArgumentException();
        Objects.requireNonNull(factory);

        T res;

        if (this.values.containsKey(arg)) {
            res = (T) this.values.get(arg);
        } else if (arg.hasDefaultValue()) {
            res = arg.getDefaultValue();
        } else {
            res = factory.get();
        }

        return res;
    }

    /**
     * Returns the value for the given {@link Option option}.
     *
     * <ol>
     *     <li>Returns the explicitly set value for the given option. (if available)</li>
     *     <li>Returns the default value for the given option. (if available)</li>
     *     <li>Returns the given {@code other} value</li>
     * </ol>
     *
     * @param <T>   the type of the option's value
     * @param opt   the option to retrieve the value for
     * @param other the alternate value
     *
     * @return the value for the given option
     *
     * @throws NullPointerException     if the given option is {@code null}
     * @throws IllegalArgumentException if the given option is <em>not</em> in the pool that this set was created from
     *
     * @since   0.1.0
     */
    @SuppressWarnings({"unchecked"})
    @Nullable
    public <T> T getOrElse(Option<T> opt, @Nullable T other) {
        if (this.pool.contains(Objects.requireNonNull(opt))) throw new IllegalArgumentException();

        T res;

        if (this.values.containsKey(opt)) {
            res = (T) this.values.get(opt);
        } else if (opt.hasDefaultValue()) {
            res = opt.getDefaultValue();
        } else {
            res = other;
        }

        return res;
    }

    /**
     * Returns the value for the given {@link Option option}.
     *
     * <ol>
     *     <li>Returns the explicitly set value for the given option. (if available)</li>
     *     <li>Returns the default value for the given option. (if available)</li>
     *     <li>Runs the given {@code factory} and returns its value</li>
     * </ol>
     *
     * @param <T>       the type of the option's value
     * @param opt       the option to retrieve the value for
     * @param factory   the factory to compute otherwise
     *
     * @return  the value for the given argument
     *
     * @throws NullPointerException     if the given option or factory is {@code null}
     * @throws IllegalArgumentException if the given option is <em>not</em> in the pool that this set was created from
     *
     * @since   0.3.0
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getOrElse(Option<T> opt, Supplier<T> factory) {
        if (this.pool.contains(Objects.requireNonNull(opt))) throw new IllegalArgumentException();
        Objects.requireNonNull(factory);

        T res;

        if (this.values.containsKey(opt)) {
            res = (T) this.values.get(opt);
        } else if (opt.hasDefaultValue()) {
            res = opt.getDefaultValue();
        } else {
            res = factory.get();
        }

        return res;
    }

    /**
     * Returns an immutable view of the dynamic option in this set.
     *
     * @return  an immutable view of dynamic options in this set
     *
     * @since   0.2.0
     */
    public Map<String, String> getDynamicOptions() {
        return this.dynamics;
    }

    /**
     * Returns whether or not a value has been set explicitly for the given {@link Argument argument}.
     *
     * @param arg   the argument to check for
     *
     * @return  {@code true} if a value has been set explicitly for the given argument, or {@code false} otherwise
     *
     * @throws NullPointerException     if the given {@code Argument} is {@code null}
     * @throws IllegalArgumentException if the given argument is <em>not</em> in the pool that this set was created from
     *
     * @since   0.1.0
     */
    public boolean isSet(Argument<?> arg) {
        if (!this.pool.contains(Objects.requireNonNull(arg))) throw new IllegalArgumentException();
        return this.values.containsKey(arg);
    }

    /**
     * Returns whether or not a value has been set explicitly for the given {@link Option option}.
     *
     * @param opt   the option to check for
     *
     * @return  {@code true} if a value has been set explicitly for the given argument, or {@code false} otherwise
     *
     * @throws NullPointerException     if the given {@code Option} is {@code null}
     * @throws IllegalArgumentException if the given option is <em>not</em> in the pool that this set was created from
     *
     * @since   0.1.0
     */
    public boolean isSet(Option<?> opt) {
        if (!this.pool.contains(Objects.requireNonNull(opt))) throw new IllegalArgumentException();
        return this.values.containsKey(opt);
    }

}