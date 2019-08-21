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
package com.github.themrmilchmann.mjl.options.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.github.themrmilchmann.mjl.options.internal.KNFFormula;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public final class KNFFormulaTests {

    private static final String A = "X";
    private static final String B = "Y";
    private static final String C = "Z";
    private static final String D = "Z";

    private static final Set<String> tau = setOf(A, B, C, D);

    @SafeVarargs
    private static <T> Set<T> setOf(T... ts) {
        Set<T> tau = new HashSet<>(Arrays.asList(ts));
        return Collections.unmodifiableSet(tau);
    }

    @Test
    public void testToBooleanString() {
        KNFFormula<String> formula = KNFFormula.builder(tau).build();
        assertEquals(formula.toBooleanString(), "()");

        formula = KNFFormula.builder(tau)
            .and(setOf(KNFFormula.Literal.neg(A), KNFFormula.Literal.pos(B)))
            .and(setOf(KNFFormula.Literal.pos(A), KNFFormula.Literal.pos(B)))
            .and(setOf(KNFFormula.Literal.pos(D), KNFFormula.Literal.pos(C), KNFFormula.Literal.neg(B)))
            .build();
        assertEquals(formula.toBooleanString(), "((\u00ACA \u2228 B) \u2227 (A \u2228 B) \u2227 (D \u2228 C \u2228 \u00ACB))");
    }

    @Test
    public void testToSetString() {
        KNFFormula<String> formula = KNFFormula.builder(tau).build();
        assertEquals(formula.toSetString(), "{}");

        formula = KNFFormula.builder(tau)
            .and(setOf(KNFFormula.Literal.neg(A), KNFFormula.Literal.pos(B)))
            .and(setOf(KNFFormula.Literal.pos(A), KNFFormula.Literal.pos(B)))
            .and(setOf(KNFFormula.Literal.pos(D), KNFFormula.Literal.pos(C), KNFFormula.Literal.neg(B)))
            .build();
        assertEquals(formula.toBooleanString(), "{{\u00ACA, B}, {A, B}, {D, C, \u00ACB}}");
    }

}