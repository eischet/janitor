// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.TestEnv;
import com.eischet.janitor.api.scopes.ScriptSource;
import com.eischet.janitor.api.scopes.VariableScope;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JMap;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An object that contains other objects shows them the way scripts see them, however deeply they are nested, so that
 * the same object has the same text everywhere.
 */
public class NestedTextTestCase extends JanitorTest {

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

    private JanitorObject withObject(final String script) throws Exception {
        return evaluate(script, globals -> globals.bind("obj", TWO_FACED));
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', textBlock = """
            return {a: obj};                                | {a=script-text}
            return {a: 1, b: obj};                          | {a=1, b=script-text}
            m = {}; m[obj] = 1; return m;                   | {script-text=1}
            return {a: [obj], b: {c: obj}};                 | {a=[script-text], b={c=script-text}}
            return [{a: obj}];                              | [{a=script-text}]
            return {a: null};                               | {a=null}
            return {};                                      | {}
            return "m: " + {a: obj};                        | m: {a=script-text}
            """)
    void mapsShowKeysAndValuesAsScriptsSeeThem(final String script, final String expected) throws Exception {
        assertEquals(expected, withObject(script).janitorToString());
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', textBlock = """
            return [obj].toSet();                           | [script-text]
            return [[obj]].toSet();                         | [[script-text]]
            return [[obj].toSet()];                         | [[script-text]]
            return {a: [obj].toSet()};                      | {a=[script-text]}
            return [null].toSet();                          | [null]
            return [].toSet();                              | []
            return "s: " + [obj].toSet();                   | s: [script-text]
            """)
    void setsShowTheirElementsAsScriptsSeeThem(final String script, final String expected) throws Exception {
        assertEquals(expected, withObject(script).janitorToString());
    }

    @Test
    void printShowsNestedObjectsAsScriptsSeeThem() throws Exception {
        assertEquals("{a=script-text}\n[script-text]\n",
                getOutput("print({a: obj}); print([obj].toSet());", globals -> globals.bind("obj", TWO_FACED)));
    }

    @Test
    void readonlyMapsShowValuesAsScriptsSeeThem() throws Exception {
        final JanitorObject readonly = ((JMap) withObject("return {a: obj};")).readonlyView();
        assertTrue(readonly.janitorToString().startsWith("{a=script-text}"), readonly.janitorToString());
    }

    @Test
    void aLargeMapShowsEveryValueAsScriptsSeeIt() throws Exception {
        final String text = withObject("m = {}; for (i from 1 to 5000) { m[i] = obj; } return m;").janitorToString();
        assertEquals(5000, text.split("script-text", -1).length - 1);
        assertEquals(-1, text.indexOf("java-text"));
    }

    @Test
    void scopesShowTheirVariablesAsScriptsSeeThem() {
        final VariableScope scope = VariableScope.createGlobalScope(TestEnv.env, ScriptSource.unnamed(""));
        assertEquals("{}", scope.janitorToString());
        scope.bind("obj", TWO_FACED);
        assertEquals("{obj=script-text}", scope.janitorToString());
    }

}
