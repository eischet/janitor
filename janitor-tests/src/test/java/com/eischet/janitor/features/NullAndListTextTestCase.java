// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.types.builtin.JNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The text of null is "null" wherever it shows up, also inside collections.
 */
public class NullAndListTextTestCase extends JanitorTest {

    @Test
    void nullHasOneText() {
        assertEquals("null", JNull.NULL.janitorToString());
        assertEquals("null", JNull.NULL.toString());
        assertEquals("value: null", "value: " + JNull.NULL);
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', textBlock = """
            return [null];                                  | [null]
            return [1, null, 'a', null];                    | [1, null, a, null]
            return [[null], [null, [null]]];                | [[null], [null, [null]]]
            return {a: null};                               | {a=null}
            return {a: [null]};                             | {a=[null]}
            return [{a: null}];                             | [{a=null}]
            r = []; for (x in [null]) { r.add(x); } return r; | [null]
            return "" + [null];                             | [null]
            """)
    void nullInsideCollectionsIsShownAsNull(final String script, final String expected) throws Exception {
        assertEquals(expected, evaluate(script).janitorToString());
    }

    @Test
    void printShowsNullInsideCollections() throws Exception {
        assertEquals("[null, 1]\n{a=null}\n", getOutput("print([null, 1]); print({a: null});"));
    }

    @Test
    void manyNullsInAList() throws Exception {
        final String text = evaluate("r = []; for (i from 1 to 10000) { r.add(null); } return r;").janitorToString();
        assertEquals("[" + "null, ".repeat(9999) + "null]", text);
    }

}
