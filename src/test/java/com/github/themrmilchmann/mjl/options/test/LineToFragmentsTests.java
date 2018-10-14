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
package com.github.themrmilchmann.mjl.options.test;

import com.github.themrmilchmann.mjl.options.OptionParser;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public final class LineToFragmentsTests {

    @DataProvider(name = "vectors")
    private static Object[][] vectors() {
        return new Object[][] {
            { "\"a b c d\"",                new String[] { "a b c d" } },
            { "a\"b c d\"e",                new String[] { "ab c de" } },
            { "ab\\\"cd",                   new String[] { "ab\"cd" } },
            { "\"a b c d\\\\\"",            new String[] { "a b c d\\" } },
            { "ab\\\\\\\"cd",               new String[] { "ab\\\"cd" } },
            { "a\\\\\\c",                   new String[] { "a\\\\\\c" } },
            { "\"a\\\\\\d\"",               new String[] { "a\\\\\\d" } },
            { "\"a b c\" d e",              new String[] { "a b c", "d", "e" } },
            { "\"ab\\\"c\"  \"\\\\\"  d",   new String[] { "ab\"c", "\\", "d" } },
            { "a\\\\\\c d\"e f\"g h",       new String[] { "a\\\\\\c", "de fg", "h" } },
            { "a\\\\\\\"b c d", new String[] { "a\\\"b", "c", "d" } }
        };
    }

    @Test(dataProvider = "vectors")
    public void testLineToFragments(String line, String[] expected) {
        String[] fragments = OptionParser.lineToFragments(line);
        assertEquals(fragments, expected);
    }

}