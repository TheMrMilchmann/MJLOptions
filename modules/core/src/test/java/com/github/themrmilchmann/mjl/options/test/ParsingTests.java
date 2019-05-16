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
package com.github.themrmilchmann.mjl.options.test;

import java.util.StringJoiner;
import com.github.themrmilchmann.mjl.options.Argument;
import com.github.themrmilchmann.mjl.options.Option;
import com.github.themrmilchmann.mjl.options.OptionParser;
import com.github.themrmilchmann.mjl.options.OptionPool;
import com.github.themrmilchmann.mjl.options.OptionSet;
import com.github.themrmilchmann.mjl.options.ParsingException;
import com.github.themrmilchmann.mjl.options.ValueParser;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public final class ParsingTests {

    private static final String TEST_GROUPS_PARSING = "parsing";

    private OptionPool argPool, optPool;

    private Argument<String> arg0;

    private Option<String> optRegular,      optRegularAlt,
                           optMarker,       optMarkerAlt,
                           optMarkerOnly,   optMarkerOnlyAlt;

    @BeforeGroups(TEST_GROUPS_PARSING)
    private void initParsingTests() {
        this.argPool = OptionPool.builder()
            .withArg(this.arg0 = Argument.builder(ValueParser.STRING, true).build())
            .build();

        this.optPool = OptionPool.builder()
            .withOption(this.optRegular = Option.builder("regular", ValueParser.STRING).withShortToken('r').build())
            .withOption(this.optMarker = Option.builder("marker", ValueParser.STRING).withShortToken('m').withMarkerValue("markerValue").build())
            .withOption(this.optMarkerOnly = Option.builder("markerOnly", ValueParser.STRING).withShortToken('o').withMarkerValue("markerValue", true).build())
            .withOption(this.optRegularAlt = Option.builder("regularAlt", ValueParser.STRING).withShortToken('s').build())
            .withOption(this.optMarkerAlt = Option.builder("markerAlt", ValueParser.STRING).withShortToken('n').withMarkerValue("markerValueAlt").build())
            .withOption(this.optMarkerOnlyAlt = Option.builder("markerOnlyAlt", ValueParser.STRING).withShortToken('p').withMarkerValue("markerValueAlt", true).build())
            .build();
    }

    @DataProvider(name = "value0")
    private static Object[][] dataValue0() {
        return new Object[][] { { ParseFun.PARSE }, { ParseFun.PARSE_LINE } };
    }

    @DataProvider(name = "value1")
    private static Object[][] dataValue1() {
        return new Object[][] {
            { ParseFun.PARSE, "value" },
            { ParseFun.PARSE_LINE, "value" },
            { ParseFun.PARSE, "longer value" },
            { ParseFun.PARSE_LINE, "longer value" },
            { ParseFun.PARSE, ""  /* empty value */ },
            { ParseFun.PARSE_LINE, ""  /* empty value */ }
        };
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value0")
    public void sec32ParseRegular_MarkerUse(ParseFun parseFun) {
        expectThrows(ParsingException.class, () -> parseFun.parse(optPool, "--regular"));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec32ParseRegular_EqualsValue(ParseFun parseFun, String value) {
        OptionSet set = parseFun.parse(optPool, "--regular=" + value);
        assertEquals(set.get(optRegular), value);
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec32ParseRegular_WhitespaceValue(ParseFun parseFun, String value) {
        OptionSet set = parseFun.parse(optPool, "--regular", value);
        assertEquals(set.get(optRegular), value);
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value0")
    public void sec32ParseMarker_MarkerUse(ParseFun parseFun) {
        OptionSet set = parseFun.parse(optPool, "--marker");
        assertEquals(set.get(optMarker), "markerValue");
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec32ParseMarker_EqualsValue(ParseFun parseFun, String value) {
        OptionSet set = parseFun.parse(optPool, "--marker=" + value);
        assertEquals(set.get(optMarker), value);
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec32ParseMarker_WhitespaceValue(ParseFun parseFun, String value) {
        OptionSet set = parseFun.parse(optPool, "--marker", value);
        assertEquals(set.get(optMarker), value);
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value0")
    public void sec32ParseMarkerOnly_MarkerUse(ParseFun parseFun) {
        OptionSet set = parseFun.parse(optPool, "--markerOnly");
        assertEquals(set.get(optMarkerOnly), "markerValue");
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec32ParseMarkerOnly_EqualsValue(ParseFun parseFun, String value) {
        expectThrows(ParsingException.class, () -> parseFun.parse(optPool, new String[] { "--markerOnly=" + value }));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec32ParseMarkerOnly_WhitespaceValue(ParseFun parseFun, String value) {
        expectThrows(ParsingException.class, () -> parseFun.parse(optPool, new String[] { "--markerOnly", value }));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value0")
    public void sec33ParseRegular_MarkerUse(ParseFun parseFun) {
        expectThrows(ParsingException.class, () -> parseFun.parse(optPool, new String[] { "-r" }));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseRegular_EqualsValue(ParseFun parseFun, String value) {
        OptionSet set = parseFun.parse(optPool, "-r=" + value);
        assertEquals(set.get(optRegular), value);
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseRegular_WhitespaceValue(ParseFun parseFun, String value) {
        OptionSet set = parseFun.parse(optPool, "-r", value);
        assertEquals(set.get(optRegular), value);
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value0")
    public void sec33ParseMarker_MarkerUse(ParseFun parseFun) {
        String[] command = { "-m" };
        OptionSet set = parseFun.parse(optPool, command);
        assertEquals(set.get(optMarker), "markerValue");
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseMarker_EqualsValue(ParseFun parseFun, String value) {
        String[] command = { "-m=" + value };
        OptionSet set = parseFun.parse(optPool, command);
        assertEquals(set.get(optMarker), value);
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseMarker_WhitespaceValue(ParseFun parseFun, String value) {
        String[] command = { "-m", value };
        OptionSet set = parseFun.parse(optPool, command);
        assertEquals(set.get(optMarker), value);
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value0")
    public void sec33ParseMarkerOnly_MarkerUse(ParseFun parseFun) {
        OptionSet set = parseFun.parse(optPool, "-o");
        assertEquals(set.get(optMarkerOnly), "markerValue");
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseMarkerOnly_EqualsValue(ParseFun parseFun, String value) {
        expectThrows(ParsingException.class, () -> parseFun.parse(optPool, new String[] { "-o=" + value }));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseMarkerOnly_WhitespaceValue(ParseFun parseFun, String value) {
        expectThrows(ParsingException.class, () -> parseFun.parse(optPool, new String[] { "-o", value }));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value0")
    public void sec33ParseRegularChain_MarkerUse(ParseFun parseFun) {
        expectThrows(ParsingException.class, () -> parseFun.parse(optPool, new String[] { "-rs" }));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseRegularChain_EqualsValue(ParseFun parseFun, String value) {
        OptionSet set = parseFun.parse(optPool, "-rs=" + value);
        assertEquals(set.get(optRegular), value);
        assertEquals(set.get(optRegularAlt), value);
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseRegularChain_WhitespaceValue(ParseFun parseFun, String value) {
        OptionSet set = parseFun.parse(optPool, "-rs", value);
        assertEquals(set.get(optRegular), value);
        assertEquals(set.get(optRegularAlt), value);
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value0")
    public void sec33ParseMarkerChain_MarkerUse(ParseFun parseFun) {
        String[] command = { "-mn" };
        OptionSet set = parseFun.parse(optPool, command);
        assertEquals(set.get(optMarker), optMarker.getMarkerValue());
        assertEquals(set.get(optMarkerAlt), optMarkerAlt.getMarkerValue());
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseMarkerChain_EqualsValue(ParseFun parseFun, String value) {
        OptionSet set = parseFun.parse(optPool, "-mn=" + value);
        assertEquals(set.get(optMarker), value);
        assertEquals(set.get(optMarkerAlt), value);
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseMarkerChain_WhitespaceValue(ParseFun parseFun, String value) {
        OptionSet set = parseFun.parse(optPool, "-mn", value);
        assertEquals(set.get(optMarker), value);
        assertEquals(set.get(optMarkerAlt), value);
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value0")
    public void sec33ParseMarkerOnlyChain_MarkerUse(ParseFun parseFun) {
        OptionSet set = parseFun.parse(optPool, "-op");
        assertEquals(set.get(optMarkerOnly), "markerValue");
        assertEquals(set.get(optMarkerOnlyAlt), "markerValueAlt");
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseMarkerOnlyChain_EqualsValue(ParseFun parseFun, String value) {
        expectThrows(ParsingException.class, () -> parseFun.parse(optPool, "-op=" + value));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseMarkerOnlyChain_WhitespaceValue(ParseFun parseFun, String value) {
        expectThrows(ParsingException.class, () -> parseFun.parse(optPool, "-op", value));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value0")
    public void sec33ParseRegularAndMarkerOnlyChain_MarkerUse(ParseFun parseFun) {
        expectThrows(ParsingException.class, () -> parseFun.parse(optPool, "-ro"));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseRegularAndMarkerOnlyChain_EqualsValue(ParseFun parseFun, String value) {
        expectThrows(ParsingException.class, () -> parseFun.parse(optPool, new String[] { "-ro=" + value }));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseRegularAndMarkerOnlyChain_WhitespaceValue(ParseFun parseFun, String value) {
        expectThrows(ParsingException.class, () -> parseFun.parse(optPool, new String[] { "-ro", value }));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value0")
    public void sec33ParseMarkerAndRegularChain_MarkerUse(ParseFun parseFun) {
        expectThrows(ParsingException.class, () -> parseFun.parse(optPool, new String[] { "-mr" }));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseMarkerAndRegularChain_EqualsValue(ParseFun parseFun, String value) {
        String[] command = new String[] { "-mr=" + value };
        String[] refCommand = { "-r=" + value };
        OptionSet set = parseFun.parse(optPool, command);
        OptionSet refSet = parseFun.parse(optPool, refCommand);
        assertEquals(set.get(optRegular), set.get(optMarker));
        assertEquals(set.get(optRegular), refSet.get(optRegular));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseMarkerAndRegularChain_WhitespaceValue(ParseFun parseFun, String value) {
        String[] command = new String[] { "-mr", value };
        String[] refCommand = new String[] { "-r", value };
        OptionSet set = parseFun.parse(optPool, command);
        OptionSet refSet = parseFun.parse(optPool, refCommand);
        assertEquals(set.get(optRegular), set.get(optMarker));
        assertEquals(set.get(optRegular), refSet.get(optRegular));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value0")
    public void sec33ParseMarkerAndMarkerOnlyChain_MarkerUse(ParseFun parseFun) {
        String[] command = { "-mo" };
        String[] refCommand = { "-o" };
        OptionSet set = parseFun.parse(optPool, command);
        OptionSet refSet = parseFun.parse(optPool, refCommand);
        assertEquals(set.get(optMarkerOnly), refSet.get(optMarkerOnly));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseMarkerAndMarkerOnlyChain_EqualsValue(ParseFun parseFun, String value) {
        expectThrows(ParsingException.class, () -> parseFun.parse(optPool, "-mo=" + value));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseMarkerAndMarkerOnlyChain_WhitespaceValue(ParseFun parseFun, String value) {
        expectThrows(ParsingException.class, () -> parseFun.parse(optPool, "-mo", value));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value0")
    public void sec34ParseDynamicOption_MarkerUse(ParseFun parseFun) {
        OptionSet set = parseFun.parse(optPool, "-#dynamic");
        assertTrue(set.getDynamicOptions().containsKey("dynamic"));
        assertNull(set.getDynamicOptions().get("dynamic"));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec34ParseDynamicOption_EqualsValue(ParseFun parseFun, String value) {
        OptionSet set = parseFun.parse(optPool, "-#dynamic=" + value);
        assertTrue(set.getDynamicOptions().containsKey("dynamic"));
        assertEquals(set.getDynamicOptions().get("dynamic"), value);
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec34ParseDynamicOption_WhitespaceValue(ParseFun parseFun, String value) {
        String[] command = { "-#dynamic", value };
        OptionSet set = parseFun.parse(optPool, command);
        assertEquals(set.getDynamicOptions().get("dynamic"), value);
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec36ParseArgument(ParseFun parseFun, String value) {
        OptionSet set = parseFun.parse(argPool, parseFun == ParseFun.PARSE_LINE ? "\"" + value + "\"" : value);

        assertTrue(set.isSet(arg0));
        assertEquals(set.get(arg0), value);
    }

    @FunctionalInterface
    private interface ParseFun {

        ParseFun PARSE = new Impl$Parse();
        ParseFun PARSE_LINE = new Impl$ParseLine();

        OptionSet parse(OptionPool pool, String... s);

        class Impl$Parse implements ParseFun {

            @Override
            public OptionSet parse(OptionPool pool, String... s) {
                return OptionParser.parseFragments(pool, s);
            }

            @Override
            public String toString() {
                return "parseFragments";
            }

        }

        class Impl$ParseLine implements ParseFun {

            @Override
            public OptionSet parse(OptionPool pool, String... s) {
                if (s.length > 1) {
                    StringJoiner wrapped = new StringJoiner(" ");
                    for (int i = 1; i < s.length; i++) wrapped.add("\"" + s[i] + "\"");

                    return OptionParser.parseLine(pool, s[0] + " " + wrapped);
                } else {
                    if (s[0].contains("=") && !s[0].endsWith("=")) {
                        String[] split = s[0].split("=");
                        s[0] = split[0] + "=\"" + split[1] + "\"";
                    }

                    return OptionParser.parseLine(pool, s[0]);
                }
            }

            @Override
            public String toString() {
                return "parseLine";
            }

        }

    }

}