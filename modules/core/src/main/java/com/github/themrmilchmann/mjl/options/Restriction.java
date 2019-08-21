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

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import com.github.themrmilchmann.mjl.options.internal.KNFFormula;
import com.github.themrmilchmann.mjl.options.internal.RestrictionImpl;

/**
 * A {@code Restriction} models a relationship between two or more options. These relationships may be used to enforce
 * certain patterns for a given set of input options.
 *
 * <p>The set of available restrictions is not functional complete as this would allow violating the specification.</p>
 *
 * @see OptionPool.Builder#withRestriction(Restriction)
 * @see OptionParser#parseFragments(Class, MethodHandles.Lookup, String...)
 *
 * @since   0.4.0
 *
 * @author  Leon Linhart
 */
public final class Restriction {

    /**
     * If all options in {@code X} are present, all options in {@code Y} must also be present.
     *
     * @param X the triggers
     * @param Y the targets
     *
     * @return  an opaque restriction that models the requested relationship
     *
     * @since   0.4.0
     */
    public static Restriction implyPresenceOf(Set<Option<?>> X, Set<Option<?>> Y) {
        return implyPresenceOfUnless(X, Y, Collections.emptySet());
    }

    /**
     * If all options in {@code X} are present, all options in {@code Y} must also be present unless all options in
     * {@code Z} are present.
     *
     * @param X the triggers
     * @param Y the targets
     * @param Z the limiters
     *
     * @return  an opaque restriction that models the requested relationship
     *
     * @since   0.4.0
     */
    public static Restriction implyPresenceOfUnless(Set<Option<?>> X, Set<Option<?>> Y, Set<Option<?>> Z) {
        return new Restriction(new RestrictionImpl.ImplyPresence(X, Y, Z, true));
    }

    /**
     * If all options in {@code X} are present, all options in {@code Y} must be absent.
     *
     * @param X the triggers
     * @param Y the targets
     *
     * @return  an opaque restriction that models the requested relationship
     *
     * @since   0.4.0
     */
    public static Restriction implyAbsenceOf(Set<Option<?>> X, Set<Option<?>> Y) {
        return implyAbsenceOfUnless(X, Y, Collections.emptySet());
    }

    /**
     * If all options in {@code X} are present, all options in {@code Y} must be absent unless all options in {@code Z}
     * are present.
     *
     * @param X the triggers
     * @param Y the targets
     * @param Z the limiters
     *
     * @return  an opaque restriction that models the requested relationship
     *
     * @since   0.4.0
     */
    public static Restriction implyAbsenceOfUnless(Set<Option<?>> X, Set<Option<?>> Y, Set<Option<?>> Z) {
        return new Restriction(new RestrictionImpl.ImplyPresence(X, Y, Z, false));
    }

    /**
     * If one of the options in {@code X} is present, all others must be absent.
     *
     * @param X the options
     *
     * @return  an opaque restriction that models the requested relationship
     *
     * @since   0.4.0
     */
    public static Restriction mutuallyExclude(Option<?>... X) {
        return new Restriction(new RestrictionImpl.MutualImplication(Arrays.stream(X).collect(Collectors.toSet()), false));
    }

    /**
     * If one of the options in {@code X} is present, all others must also be.
     *
     * @param X the options
     *
     * @return  an opaque restriction that models the requested relationship
     *
     * @since   0.4.0
     */
    public static Restriction mutuallyRequire(Option<?>... X) {
        return new Restriction(new RestrictionImpl.MutualImplication(Arrays.stream(X).collect(Collectors.toSet()), true));
    }

    private final RestrictionImpl impl;

    private Restriction(RestrictionImpl impl) {
        this.impl = impl;
    }

    /**
     * Returns whether this restriction applies to the given set of options.
     *
     * @param options   the set of options to check this restriction against
     *
     * @return  whether this restriction applies to the given set of options
     *
     * @since   0.4.0
     */
    public boolean appliesTo(Set<Option<?>> options) {
        return this.impl.appliesTo(options);
    }

    /**
     * Returns whether this restriction is violated in the given set of options.
     *
     * @param options   the set of options to check this restriction against
     *
     * @return  whether this restriction is violated in the given set of options
     *
     * @since   0.4.0
     */
    public boolean isViolatedBy(Set<Option<?>> options) {
        return this.impl.isViolatedBy(options);
    }

    /**
     * All options referred to by this restriction.
     *
     * @return  all options referred to by this restriction
     *
     * @since   0.4.0
     */
    public Set<Option<?>> getOptions() {
        return this.impl.getOptions();
    }

    Set<Set<KNFFormula.Literal<Option<?>>>> getClauses() {
        return this.impl.getClauses();
    }

    /**
     * {@inheritDoc}
     *
     * @since   0.4.0
     */
    @Override
    public int hashCode() {
        return this.impl.hashCode();
    }

    /**
     * {@inheritDoc}
     *
     * @since   0.4.0
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj instanceof Restriction) {
            Restriction other = (Restriction) obj;
            return this.impl.equals(other.impl);
        }

        return false;
    }

}