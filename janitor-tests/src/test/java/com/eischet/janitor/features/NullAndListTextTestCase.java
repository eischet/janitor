// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JNull;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The text of null is "null" wherever it shows up, also inside collections, and a list shows its elements the way
 * scripts see them.
 */
public class NullAndListTextTestCase extends JanitorTest {

    /** A host object whose text for scripts differs from its Java text. */
    private static final JanitorObject TWO_FACED = new JanitorObject() {
        @Override
        public String toString() {
            return "java-text";
        }

        @Override
        public @NotNull String janitorToString() {
            return "script-text";
        }
    };

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

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', textBlock = """
            return [obj];                                   | [script-text]
            return [1, obj, [obj, null]];                   | [1, script-text, [script-text, null]]
            return [];                                      | []
            return "" + [obj];                              | [script-text]
            return [obj] + "";                              | [script-text]
            return "a" + [1, obj, [obj, null]] + "b";       | a[1, script-text, [script-text, null]]b
            return "x: " + {a: [obj]}.a;                    | x: [script-text]
            s = "list "; s += [obj]; return s;              | list [script-text]
            """)
    void listsShowTheirElementsAsScriptsSeeThem(final String script, final String expected) throws Exception {
        assertEquals(expected, evaluate(script, globals -> globals.bind("obj", TWO_FACED)).janitorToString());
    }

    @Test
    void printShowsListElementsAsScriptsSeeThem() throws Exception {
        assertEquals("[script-text]\n", getOutput("print([obj]);", globals -> globals.bind("obj", TWO_FACED)));
    }

}
