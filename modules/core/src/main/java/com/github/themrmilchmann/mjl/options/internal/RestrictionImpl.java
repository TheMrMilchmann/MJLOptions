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
package com.github.themrmilchmann.mjl.options.internal;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.github.themrmilchmann.mjl.options.Option;

/**
 * Implementations for various restriction types.
 *
 * @author  Leon Linhart
 */
public abstract class RestrictionImpl {

    public abstract boolean appliesTo(Set<Option<?>> options);
    public abstract boolean isViolatedBy(Set<Option<?>> options);

    public abstract Set<Set<KNFFormula.Literal<Option<?>>>> getClauses();
    public abstract Set<Option<?>> getOptions();

    public static final class ImplyPresence extends RestrictionImpl {

        private final Set<Option<?>> triggers, targets, limiters;
        private final boolean shouldBePresent;

        public ImplyPresence(Set<Option<?>> triggers, Set<Option<?>> targets, Set<Option<?>> limiters, boolean shouldBePresent) {
            this.triggers = triggers;
            this.targets = targets;
            this.limiters = limiters;
            this.shouldBePresent = shouldBePresent;
        }

        @Override
        public boolean appliesTo(Set<Option<?>> options) {
            return options.containsAll(this.triggers) && !options.containsAll(this.limiters);
        }

        @Override
        public boolean isViolatedBy(Set<Option<?>> options) {
            if (!this.appliesTo(options)) return false;
            return this.shouldBePresent ? this.targets.stream().anyMatch(it -> !options.contains(it)) : this.targets.stream().anyMatch(options::contains);
        }

        /*
         * X ^ Y => U ^ V
         * <=> -(X ^ Y) v (U ^ V)
         * <=> (-X v -Y) v (U ^ V)
         * <=> -X v -Y v (U ^ V)
         * <=> (U v -X v -Y) ^ (V v -X v -Y)
         *
         * with limiters:
         *
         * (A ^ B) v (X ^ Y => U ^ V)
         * <=> (A ^ B) v -(X ^ Y) v (U ^ V)
         * <=> (A ^ B) v (-X v -Y) v (U ^ V)
         * <=> (A ^ B) v -X v -Y v (U ^ V)
         * <=> (A v U v -X v -Y) ^ (A v V v -X v -Y) ^ (B v U v -X v -Y) ^ (B v V v -X v -Y)
         */
        @Override
        public Set<Set<KNFFormula.Literal<Option<?>>>> getClauses() {
            Set<Set<KNFFormula.Literal<Option<?>>>> clauses = new HashSet<>();

            if (this.limiters.isEmpty()) {
                for (Option<?> target : this.targets) {
                    Set<KNFFormula.Literal<Option<?>>> clause = new HashSet<>();
                    this.buildClause(clauses, target, clause);
                }
            } else {
                for (Option<?> limiter : this.limiters) {
                    for (Option<?> target : this.targets) {
                        Set<KNFFormula.Literal<Option<?>>> clause = new HashSet<>();
                        clause.add(KNFFormula.Literal.pos(limiter));
                        this.buildClause(clauses, target, clause);
                    }
                }
            }

            return clauses;
        }

        private void buildClause(Set<Set<KNFFormula.Literal<Option<?>>>> clauses, Option<?> target, Set<KNFFormula.Literal<Option<?>>> clause) {
            if (this.shouldBePresent) {
                clause.add(KNFFormula.Literal.pos(target));
            } else {
                clause.add(KNFFormula.Literal.neg(target));
            }

            this.triggers.forEach(trigger -> clause.add(KNFFormula.Literal.neg(trigger)));
            clauses.add(clause);
        }

        @Override
        public Set<Option<?>> getOptions() {
            return Stream.concat(Stream.concat(this.triggers.stream(), this.targets.stream()), this.limiters.stream()).collect(Collectors.toSet());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;

            if (obj instanceof ImplyPresence) {
                ImplyPresence other = (ImplyPresence) obj;

                return Objects.equals(this.triggers, other.triggers)
                    && Objects.equals(this.targets, other.targets)
                    && Objects.equals(this.limiters, other.limiters)
                    && this.shouldBePresent == other.shouldBePresent;
            }

            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.triggers, this.targets, this.limiters, this.shouldBePresent);
        }

    }

    public static final class MutualImplication extends RestrictionImpl {

        private final Set<Option<?>> options;
        private final boolean shouldBePresent;

        public MutualImplication(Set<Option<?>> options, boolean shouldBePresent) {
            this.options = options;
            this.shouldBePresent = shouldBePresent;
        }

        @Override
        public boolean appliesTo(Set<Option<?>> options) {
            return this.options.stream().anyMatch(options::contains);
        }

        @Override
        public boolean isViolatedBy(Set<Option<?>> options) {
            return this.options.stream().filter(options::contains).count() > 1;
        }

        /*
         * (A => B ^ C ^ D) ^ (B => A ^ C ^ D) ^ (C => A ^ B ^ D) ^ (D => A ^ B ^ C)
         * <=> (-A v (B ^ C ^ D)) ^ ...
         * <=> (-A v B) ^ (-A v C) ^ (-A v D) ^ ...
         */
        @Override
        public Set<Set<KNFFormula.Literal<Option<?>>>> getClauses() {
            Set<Set<KNFFormula.Literal<Option<?>>>> clauses = new HashSet<>();

            for (Option<?> target : this.options) {
                Set<KNFFormula.Literal<Option<?>>> clause = new HashSet<>();
                clause.add(KNFFormula.Literal.neg(target));

                for (Option<?> trigger : this.options) {
                    if (trigger == target) continue;

                    if (this.shouldBePresent) {
                        clause.add(KNFFormula.Literal.pos(trigger));
                    } else {
                        clause.add(KNFFormula.Literal.neg(trigger));
                    }
                }

                clauses.add(clause);
            }

            return clauses;
        }

        @Override
        public Set<Option<?>> getOptions() {
            return this.options;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;

            if (obj instanceof MutualImplication) {
                MutualImplication other = (MutualImplication) obj;

                return Objects.equals(this.options, other.options)
                    && this.shouldBePresent == other.shouldBePresent;
            }

            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.options, this.shouldBePresent);
        }

    }

}