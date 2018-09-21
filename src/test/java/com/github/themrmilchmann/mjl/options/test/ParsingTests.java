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

    @DataProvider(name = "value1")
    private static Object[][] dataValue1() {
        return new String[][] { { "value" }, { "longer value" }, { ""  /* empty value */ } };
    }

    private static String stripQuotes(String value) {
        if (value.startsWith("\"")) value = value.substring(1);
        if (value.endsWith("\"")) value = value.substring(0, value.length() - 1);

        return value;
    }

    @Test(groups = TEST_GROUPS_PARSING)
    public void sec32ParseRegular_MarkerUse() {
        expectThrows(ParsingException.class, () -> OptionParser.parse(optPool, "--regular"));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec32ParseRegular_EqualsValue(String value) {
        OptionSet set = OptionParser.parse(optPool, "--regular=" + value);
        assertEquals(set.get(optRegular), stripQuotes(value));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec32ParseRegular_WhitespaceValue(String value) {
        OptionSet set = OptionParser.parse(optPool, "--regular", value);
        assertEquals(set.get(optRegular), stripQuotes(value));
    }

    @Test(groups = TEST_GROUPS_PARSING)
    public void sec32ParseMarker_MarkerUse() {
        OptionSet set = OptionParser.parse(optPool, "--marker");
        assertEquals(set.get(optMarker), "markerValue");
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec32ParseMarker_EqualsValue(String value) {
        OptionSet set = OptionParser.parse(optPool, "--marker=" + value);
        assertEquals(set.get(optMarker), stripQuotes(value));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec32ParseMarker_WhitespaceValue(String value) {
        OptionSet set = OptionParser.parse(optPool, "--marker", value);
        assertEquals(set.get(optMarker), stripQuotes(value));
    }

    @Test(groups = TEST_GROUPS_PARSING)
    public void sec32ParseMarkerOnly_MarkerUse() {
        OptionSet set = OptionParser.parse(optPool, "--markerOnly");
        assertEquals(set.get(optMarkerOnly), "markerValue");
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec32ParseMarkerOnly_EqualsValue(String value) {
        expectThrows(ParsingException.class, () -> OptionParser.parse(optPool, new String[] { "--markerOnly=" + value }));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec32ParseMarkerOnly_WhitespaceValue(String value) {
        expectThrows(ParsingException.class, () -> OptionParser.parse(optPool, new String[] { "--markerOnly", value }));
    }

    @Test(groups = TEST_GROUPS_PARSING)
    public void sec33ParseRegular_MarkerUse() {
        expectThrows(ParsingException.class, () -> OptionParser.parse(optPool, new String[] { "-r" }));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseRegular_EqualsValue(String value) {
        OptionSet set = OptionParser.parse(optPool, "-r=" + value);
        assertEquals(set.get(optRegular), stripQuotes(value));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseRegular_WhitespaceValue(String value) {
        OptionSet set = OptionParser.parse(optPool, "-r", value);
        assertEquals(set.get(optRegular), stripQuotes(value));
    }

    @Test(groups = TEST_GROUPS_PARSING)
    public void sec33ParseMarker_MarkerUse() {
        String[] command = { "-m" };
        OptionSet set = OptionParser.parse(optPool, command);
        assertEquals(set.get(optMarker), "markerValue");
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseMarker_EqualsValue(String value) {
        String[] command = { "-m=" + value };
        OptionSet set = OptionParser.parse(optPool, command);
        assertEquals(set.get(optMarker), stripQuotes(value));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseMarker_WhitespaceValue(String value) {
        String[] command = { "-m", value };
        OptionSet set = OptionParser.parse(optPool, command);
        assertEquals(set.get(optMarker), stripQuotes(value));
    }

    @Test(groups = TEST_GROUPS_PARSING)
    public void sec33ParseMarkerOnly_MarkerUse() {
        OptionSet set = OptionParser.parse(optPool, "-o");
        assertEquals(set.get(optMarkerOnly), "markerValue");
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseMarkerOnly_EqualsValue(String value) {
        expectThrows(ParsingException.class, () -> OptionParser.parse(optPool, new String[] { "-o=" + value }));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseMarkerOnly_WhitespaceValue(String value) {
        expectThrows(ParsingException.class, () -> OptionParser.parse(optPool, new String[] { "-o", value }));
    }

    @Test(groups = TEST_GROUPS_PARSING)
    public void sec33ParseRegularChain_MarkerUse() {
        expectThrows(ParsingException.class, () -> OptionParser.parse(optPool, new String[] { "-rs" }));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseRegularChain_EqualsValue(String value) {
        OptionSet set = OptionParser.parse(optPool, "-rs=" + value);
        assertEquals(set.get(optRegular), stripQuotes(value));
        assertEquals(set.get(optRegularAlt), stripQuotes(value));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseRegularChain_WhitespaceValue(String value) {
        OptionSet set = OptionParser.parse(optPool, "-rs", value);
        assertEquals(set.get(optRegular), stripQuotes(value));
        assertEquals(set.get(optRegularAlt), stripQuotes(value));
    }

    @Test(groups = TEST_GROUPS_PARSING)
    public void sec33ParseMarkerChain_MarkerUse() {
        String[] command = { "-mn" };
        OptionSet set = OptionParser.parse(optPool, command);
        assertEquals(set.get(optMarker), optMarker.getMarkerValue());
        assertEquals(set.get(optMarkerAlt), optMarkerAlt.getMarkerValue());
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseMarkerChain_EqualsValue(String value) {
        OptionSet set = OptionParser.parse(optPool, "-mn=" + value);
        assertEquals(set.get(optMarker), stripQuotes(value));
        assertEquals(set.get(optMarkerAlt), stripQuotes(value));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseMarkerChain_WhitespaceValue(String value) {
        OptionSet set = OptionParser.parse(optPool, "-mn", value);
        assertEquals(set.get(optMarker), stripQuotes(value));
        assertEquals(set.get(optMarkerAlt), stripQuotes(value));
    }

    @Test(groups = TEST_GROUPS_PARSING)
    public void sec33ParseMarkerOnlyChain_MarkerUse() {
        OptionSet set = OptionParser.parse(optPool, "-op");
        assertEquals(set.get(optMarkerOnly), "markerValue");
        assertEquals(set.get(optMarkerOnlyAlt), "markerValueAlt");
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseMarkerOnlyChain_EqualsValue(String value) {
        expectThrows(ParsingException.class, () -> OptionParser.parse(optPool, "-op=" + value));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseMarkerOnlyChain_WhitespaceValue(String value) {
        expectThrows(ParsingException.class, () -> OptionParser.parse(optPool, "-op", value));
    }

    @Test(groups = TEST_GROUPS_PARSING)
    public void sec33ParseRegularAndMarkerOnlyChain_MarkerUse() {
        expectThrows(ParsingException.class, () -> OptionParser.parse(optPool, "-ro"));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseRegularAndMarkerOnlyChain_EqualsValue(String value) {
        expectThrows(ParsingException.class, () -> OptionParser.parse(optPool, new String[] { "-ro=" + value }));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseRegularAndMarkerOnlyChain_WhitespaceValue(String value) {
        expectThrows(ParsingException.class, () -> OptionParser.parse(optPool, new String[] { "-ro", value }));
    }

    @Test(groups = TEST_GROUPS_PARSING)
    public void sec33ParseMarkerAndRegularChain_MarkerUse() {
        expectThrows(ParsingException.class, () -> OptionParser.parse(optPool, new String[] { "-mr" }));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseMarkerAndRegularChain_EqualsValue(String value) {
        String[] command = new String[] { "-mr=" + value };
        String[] refCommand = { "-r=" + value };
        OptionSet set = OptionParser.parse(optPool, command);
        OptionSet refSet = OptionParser.parse(optPool, refCommand);
        assertEquals(set.get(optRegular), set.get(optMarker));
        assertEquals(set.get(optRegular), refSet.get(optRegular));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseMarkerAndRegularChain_WhitespaceValue(String value) {
        String[] command = new String[] { "-mr", value };
        String[] refCommand = new String[] { "-r", value };
        OptionSet set = OptionParser.parse(optPool, command);
        OptionSet refSet = OptionParser.parse(optPool, refCommand);
        assertEquals(set.get(optRegular), set.get(optMarker));
        assertEquals(set.get(optRegular), refSet.get(optRegular));
    }

    @Test(groups = TEST_GROUPS_PARSING)
    public void sec33ParseMarkerAndMarkerOnlyChain_MarkerUse() {
        String[] command = { "-mo" };
        String[] refCommand = { "-o" };
        OptionSet set = OptionParser.parse(optPool, command);
        OptionSet refSet = OptionParser.parse(optPool, refCommand);
        assertEquals(set.get(optMarkerOnly), refSet.get(optMarkerOnly));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseMarkerAndMarkerOnlyChain_EqualsValue(String value) {
        expectThrows(ParsingException.class, () -> OptionParser.parse(optPool, "-mo=" + value));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec33ParseMarkerAndMarkerOnlyChain_WhitespaceValue(String value) {
        expectThrows(ParsingException.class, () -> OptionParser.parse(optPool, "-mo", value));
    }

    @Test(groups = TEST_GROUPS_PARSING)
    public void sec34ParseDynamicOption_MarkerUse() {
        OptionSet set = OptionParser.parse(optPool, "-$dynamic");
        assertTrue(set.getDynamicOptions().containsKey("dynamic"));
        assertNull(set.getDynamicOptions().get("dynamic"));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec34ParseDynamicOption_EqualsValue(String value) {
        OptionSet set = OptionParser.parse(optPool, "-$dynamic=" + value);
        assertTrue(set.getDynamicOptions().containsKey("dynamic"));
        assertEquals(set.getDynamicOptions().get("dynamic"), stripQuotes(value));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec34ParseDynamicOption_WhitespaceValue(String value) {
        expectThrows(ParsingException.class, () -> OptionParser.parse(optPool, "-$dynamic", value));
    }

    @Test(groups = TEST_GROUPS_PARSING, dataProvider = "value1")
    public void sec36ParseArgument(String value) {
        OptionSet set = OptionParser.parse(argPool, value);

        assertTrue(set.isSet(arg0));
        assertEquals(set.get(arg0), stripQuotes(value));
    }

}