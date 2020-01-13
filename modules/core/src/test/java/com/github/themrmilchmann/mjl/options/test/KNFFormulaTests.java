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
package com.github.themrmilchmann.mjl.options.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.github.themrmilchmann.mjl.options.internal.KNFFormula;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public final class KNFFormulaTests {

    private static final String A = "A";
    private static final String B = "B";
    private static final String C = "C";
    private static final String D = "D";

    private static final List<String> tau = listOf(A, B, C, D);

    @SafeVarargs
    private static <T> List<T> listOf(T... ts) {
        List<T> tau = Arrays.asList(ts);
        return Collections.unmodifiableList(tau);
    }

    @Test
    public void testToBooleanString() {
        KNFFormula<String> formula = KNFFormula.builder(tau, false).build();
        assertEquals(formula.toBooleanString(), "()");

        formula = KNFFormula.builder(tau, false)
            .and(listOf(KNFFormula.Literal.neg(A), KNFFormula.Literal.pos(B)))
            .and(listOf(KNFFormula.Literal.pos(A), KNFFormula.Literal.pos(B)))
            .and(listOf(KNFFormula.Literal.pos(D), KNFFormula.Literal.pos(C), KNFFormula.Literal.neg(B)))
            .build();
        assertEquals(formula.toBooleanString(), "((\u00ACA \u2228 B) \u2227 (A \u2228 B) \u2227 (D \u2228 C \u2228 \u00ACB))");
    }

    @Test
    public void testToSetString() {
        KNFFormula<String> formula = KNFFormula.builder(tau, false).build();
        assertEquals(formula.toSetString(), "{}");

        formula = KNFFormula.builder(tau, false)
            .and(listOf(KNFFormula.Literal.neg(A), KNFFormula.Literal.pos(B)))
            .and(listOf(KNFFormula.Literal.pos(A), KNFFormula.Literal.pos(B)))
            .and(listOf(KNFFormula.Literal.pos(D), KNFFormula.Literal.pos(C), KNFFormula.Literal.neg(B)))
            .build();
        assertEquals(formula.toSetString(), "{{\u00ACA, B}, {A, B}, {D, C, \u00ACB}}");
    }

}