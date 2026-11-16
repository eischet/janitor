// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.types;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Short string literals share one instance, while the strings a script computes at run time stay out of the JVM's
 * string table: a script that builds many strings must not pay a string table lookup for each of them.
 */
public class JStringInterningTestCase extends JanitorTest {

    @Test
    void literalsAreInterned() throws JanitorCompilerException, JanitorRuntimeException {
        final Object value = evaluate("\"Hallo\";").janitorGetHostValue();
        assertSame("Hallo", value);
    }

    @Test
    void computedStringsAreNotInterned() throws JanitorCompilerException, JanitorRuntimeException {
        "Hallo x".intern(); // make sure the table holds the value, so interning would return exactly this instance
        for (final String script : new String[] {
                "titel = \"Hallo\"; titel + \" \" + \"x\";",
                "\"hallo x\".toUpperCase().toLowerCase().replace(\"h\", \"H\");",
                "\"%s %s\".format(\"Hallo\", \"x\");",
        }) {
            final Object value = evaluate(script).janitorGetHostValue();
            assertEquals("Hallo x", value, script);
            assertNotSame("Hallo x", value, script);
        }
    }

}
