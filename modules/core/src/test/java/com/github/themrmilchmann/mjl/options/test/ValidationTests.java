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

import com.github.themrmilchmann.mjl.options.Argument;
import com.github.themrmilchmann.mjl.options.Option;
import com.github.themrmilchmann.mjl.options.ValueParser;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public final class ValidationTests {

    private static final String TEST_GROUPS_VALIDATION = "validation";

    @Test(groups = TEST_GROUPS_VALIDATION)
    public void implValidateArgumentBuilder_NPE() {
        expectThrows(NullPointerException.class, () -> Argument.builder(null));
    }

    @Test(groups = TEST_GROUPS_VALIDATION)
    public void implValidateOptionBuilder_NPE() {
        expectThrows(NullPointerException.class, () -> Option.builder("token", null));
        expectThrows(NullPointerException.class, () -> Option.builder(null, ValueParser.STRING));
    }

    @Test(groups = TEST_GROUPS_VALIDATION)
    public void sec2ValidateLongOptionToken() {
        assertNotNull(Option.builder("valid.long-tokenT35t", ValueParser.STRING));
        assertNotNull(Option.builder("VAliwfw874z72-w.-2.3a--.", ValueParser.STRING));
        assertNotNull(Option.builder("valid.loWubdwvzawdng-tokenT35t..", ValueParser.STRING));
   }

    @Test(groups = TEST_GROUPS_VALIDATION)
    public void sec2ValidateLongOptionToken_Empty() {
        expectThrows(IllegalArgumentException.class, () -> Option.builder("", ValueParser.STRING));
    }

    @Test(groups = TEST_GROUPS_VALIDATION)
    public void sec2ValidateLongOptionToken_InitialHyphen() {
        expectThrows(IllegalArgumentException.class, () -> Option.builder("-invalid", ValueParser.STRING));
    }

    @Test(groups = TEST_GROUPS_VALIDATION)
    public void sec2ValidateLongOptionToken_InitialDot() {
        expectThrows(IllegalArgumentException.class, () -> Option.builder(".invalid", ValueParser.STRING));
    }

    @Test(groups = TEST_GROUPS_VALIDATION)
    public void sec2ValidateShortOptionToken() {
        assertNotNull(Option.builder("token", ValueParser.STRING).withShortToken('A'));
        assertNotNull(Option.builder("token", ValueParser.STRING).withShortToken('a'));
        assertNotNull(Option.builder("token", ValueParser.STRING).withShortToken('z'));
        expectThrows(IllegalArgumentException.class, () -> Option.builder("token", ValueParser.STRING).withShortToken('0'));
        expectThrows(IllegalArgumentException.class, () -> Option.builder("token", ValueParser.STRING).withShortToken('.'));
    }

}