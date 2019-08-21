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

import com.github.themrmilchmann.mjl.options.ParsingException;
import com.github.themrmilchmann.mjl.options.ValueParser;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public final class ValueParserTests {

    @SuppressWarnings("ConstantConditions")
    @Test
    public static void testValueParser$BOOLEAN() {
        ValueParser<Boolean> parser = ValueParser.BOOLEAN;
        assertTrue(parser.parse("true"));
        assertTrue(parser.parse("True"));
        assertTrue(parser.parse("tRuE"));
        assertTrue(parser.parse("1"));
        assertFalse(parser.parse("0"));
        assertFalse(parser.parse("Wackelpudding"));
    }

    @Test
    public static void testValueParser$CHARACTER() {
        ValueParser<Character> parser = ValueParser.CHARACTER;
        assertEquals(parser.parse("w").charValue(), 'w');
        assertThrows(ParsingException.class, () -> parser.parse("Butterstute"));
    }

}