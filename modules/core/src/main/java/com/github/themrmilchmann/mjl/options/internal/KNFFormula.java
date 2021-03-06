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
package com.github.themrmilchmann.mjl.options.internal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class KNFFormula<T> {

    public static <T> KNFFormula.Builder<T> builder(Collection<T> variables) {
        return builder(variables, true);
    }

    public static <T> KNFFormula.Builder<T> builder(Collection<T> variables, boolean varsToClauses) {
        return new KNFFormula.Builder<>(variables, varsToClauses);
    }

    private final List<List<Literal<T>>> clauses;

    private KNFFormula(List<List<Literal<T>>> clauses) {
        this.clauses = clauses;
    }

    private Set<T> calculateUnreachableOptions(Set<T> unreachable) {
        modifiedDPLL(this.clauses, unreachable);
        return unreachable;
    }

    public String toBooleanString() {
        return "(" + this.toString(" \u2227 ", KNFFormula::clauseToBooleanString) + ")";
    }

    public String toSetString() {
        return "{" + this.toString(", ", KNFFormula::clauseToSetString) + "}";
    }

    @Override
    public String toString() {
        return this.toBooleanString();
    }

    private String toString(String delimiter, Function<List<Literal<T>>, String> conv) {
        StringJoiner stringJoiner = new StringJoiner(delimiter);
        this.clauses.forEach(clause -> stringJoiner.add(conv.apply(clause)));

        return stringJoiner.toString();
    }

    /*
     * A modified version of the DPLL algorithm that checks for a given formula if there is at least one
     * interpretation J for each variable X in which J(X) = 1.
     *
     * (Thus the algorithm is technically a _harder_ satisfiability check.)
     */
    private static <T> boolean modifiedDPLL(List<List<Literal<T>>> clauses, Set<T> unreachable) {
        Deque<Pair<List<List<Literal<T>>>, Set<T>>> stack = new ArrayDeque<>();
        stack.push(new Pair<>(clauses, unreachable));

        while (!stack.isEmpty()) {
            Pair<List<List<Literal<T>>>, Set<T>> pair = stack.pop();
            List<List<Literal<T>>> localClauses = pair.first;
            Set<T> localUnreachable = pair.second;

            if (localClauses.isEmpty()) {
                /*
                 * If the formula is satisfiable under the current assumption, the options reachable under this
                 * assumption are reachable in at least one valid model. Thus the - now - reachable options need
                 * to be propagated upwards.
                 */
                unreachable.removeIf(var -> !localUnreachable.contains(var));
                if (unreachable.isEmpty()) return true;

                continue;
            }

            if (localClauses.stream().anyMatch(List::isEmpty)) continue;

            Optional<List<Literal<T>>> unitClause;
            Optional<Literal<T>> pureLiteral;

            if ((unitClause = localClauses.parallelStream().filter(clause -> clause.size() == 1).findAny()).isPresent()) {
                /*
                 * If there is only a single literal in a clause, the literal needs to be true for the formula to be
                 * clause (and thus the formula) to be satisfiable.
                 *
                 * As a consequence all clauses containing the literal can be eliminated and the compliment of the
                 * literal can be eliminated from all remaining clauses.
                 */
                List<Literal<T>> clause = unitClause.get();

                @SuppressWarnings("OptionalGetWithoutIsPresent")
                Literal<T> literal = clause.stream().findFirst().get();

                if (literal.pos) localUnreachable.remove(literal.var);

                localClauses.removeIf(itClause -> itClause.contains(literal));
                localClauses.forEach(itClause -> itClause.remove(literal.compliment()));
                stack.push(pair);
            } else if ((pureLiteral = localClauses.stream()
                // Create a stream of literals
                .flatMap(List::stream)
                // Filter literals that are in more than one clause
                .distinct()
                // Collect all literals by their variable
                .collect(Collectors.groupingBy(literal -> literal.var))
                .values()
                .stream()
                // Filter all variables that are only present in one literal (= that exist in pure form)
                .filter(literals -> literals.size() == 1)
                .map(literals -> literals.get(0))
                .findAny()
            ).isPresent()) {
                Literal<T> literal = pureLiteral.get();
                if (literal.pos) localUnreachable.remove(literal.var);

                localClauses.removeIf(clause -> clause.contains(literal));
                stack.push(pair);
            } else {
                Optional<Map.Entry<List<Literal<T>>, Long>> shortestClause = localClauses.stream()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting())).entrySet().stream()
                    .min((a, b) -> Long.compare(b.getValue(), a.getValue()));

                @SuppressWarnings("OptionalGetWithoutIsPresent")
                Literal<T> literal = shortestClause.get().getKey().stream().findFirst().get();

                for (boolean value : new boolean[] { true, false }) {
                    List<List<Literal<T>>> _clauses = localClauses.stream().map(ArrayList::new).collect(Collectors.toList());
                    Set<T> _unreachable = new HashSet<>(localUnreachable);

                    if (value) {
                        // Assume the literal is true
                        _clauses.removeIf(clause -> clause.contains(literal));
                        _clauses.forEach(clause -> clause.remove(literal.compliment()));
                    } else {
                        // Assume the literal is false
                        _clauses.removeIf(clause -> clause.contains(literal.compliment()));
                        _clauses.forEach(clause -> clause.remove(literal));
                    }

                    stack.push(new Pair<>(_clauses, _unreachable));
                }
            }
        }

        return false;
    }

    public static <T> String clauseToBooleanString(List<Literal<T>> clause) {
        return "(" + clauseToString(clause, " \u2228 ") + ")";
    }

    public static <T> String clauseToSetString(List<Literal<T>> clause) {
        return "{" + clauseToString(clause, ", ") + "}";
    }

    private static <T> String clauseToString(List<Literal<T>> clause, String delimiter) {
        StringJoiner stringJoiner = new StringJoiner(delimiter);
        clause.forEach(literal -> stringJoiner.add(literal.toString()));

        return stringJoiner.toString();
    }

    public static final class Builder<T> {

        private final List<List<Literal<T>>> clauses = new ArrayList<>();
        private final Collection<T> vars;

        private Builder(Collection<T> vars, boolean varsToClauses) {
            this.vars = vars;
            if (varsToClauses) vars.stream().map(Literal::pos).map(Collections::singletonList).forEach(this::and);
        }

        public Builder<T> and(List<Literal<T>> clause) {
            this.clauses.add(clause);
            return this;
        }

        public Builder<T> and(Set<Literal<T>> clause) {
            this.clauses.add(new ArrayList<>(clause));
            return this;
        }

        public KNFFormula<T> build() {
            return new KNFFormula<>(this.clauses.stream().map(ArrayList::new).collect(Collectors.toList()));
        }

        public Set<T> calculateUnreachableOptions() {
            KNFFormula<T> formula = this.build();
            return formula.calculateUnreachableOptions(new HashSet<>(this.vars));
        }

    }

    public static final class Literal<T> {

        public static <T> Literal<T> pos(T var) {
            return new Literal<>(var, true);
        }

        public static <T> Literal<T> neg(T var) {
            return new Literal<>(var, false);
        }

        private T var;
        private boolean pos;

        private Literal(T var, boolean pos) {
            this.var = var;
            this.pos = pos;
        }

        public Literal<T> compliment() {
            return new Literal<>(var, !this.pos);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.var, this.pos);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;

            if (obj instanceof Literal) {
                Literal<?> other = (Literal<?>) obj;
                return Objects.equals(this.var, other.var)
                    && this.pos == other.pos;
            }

            return false;
        }

        @Override
        public String toString() {
            return this.pos ? this.var.toString() : "\u00AC" + this.var.toString();
        }

    }

    private static final class Pair<L, R> {

        private final L first;
        private final R second;

        private Pair(L first, R second) {
            this.first = first;
            this.second = second;
        }

    }

}