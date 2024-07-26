package com.eischet.janitor;

import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test common string operations and methods.
 */
public class StringClassTestCase {

    private void testStringMethod(final String script, final Object expectedResult) throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        final RunnableScript runnableScript = rt.compile("test", script);
        final @NotNull JanitorObject result = runnableScript.run();
        final Object actualResult = result.janitorGetHostValue();
        assertEquals(expectedResult, actualResult, script);
    }

    private void testStringMethod(final String script, final byte[] expectedResult) throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = new OutputCatchingTestRuntime();
        final RunnableScript runnableScript = rt.compile("test", script);
        final @NotNull JanitorObject result = runnableScript.run();
        final Object actualResult = result.janitorGetHostValue();
        if (actualResult instanceof byte[]) {
            assertArrayEquals(expectedResult, (byte[]) actualResult, script);
        } else {
            assertEquals(expectedResult, actualResult, script);
        }
        // assertArrayEquals(expectedResult, (byte[]) actualResult, script);
    }


    @Test
    void testStringMethods() throws JanitorCompilerException, JanitorRuntimeException {
        // String length
        testStringMethod("'foobar'.length()", 6L);
        testStringMethod("''.length()", 0L);

        // String trimming
        testStringMethod("'foobar'.trim()", "foobar");
        testStringMethod("'  foobar  '.trim()", "foobar");

        // Contains / Index Of / Sub Strings...
        testStringMethod("'foobar'.contains('bar')", true);
        testStringMethod("'foobar'.contains('baz')", false);
        testStringMethod("'foobar'.containsIgnoreCase('BAR')", true);
        testStringMethod("'foobar'.containsIgnoreCase('BAZ')", false);
        testStringMethod("'foobar'.indexOf('bar')", 3L);
        testStringMethod("'foobar'.indexOf('baz')", -1L);
        testStringMethod("'foobar'.empty()", false);
        testStringMethod("''.empty()", true);
        testStringMethod("'foobar'.startsWith('foo')", true);
        testStringMethod("'foobar'.startsWith('bar')", false);
        testStringMethod("'foobar'.endsWith('bar')", true);
        testStringMethod("'foobar'.endsWith('foo')", false);
        testStringMethod("'foobar'.substring(3)", "bar");
        testStringMethod("'foobar'.substring(3, 5)", "ba");
        testStringMethod("'foobar'.count('o')", 2L);
        testStringMethod("'foobar'.get(3)", "b");


        // Replacing / Changing...
        testStringMethod("'foobar'.replaceAll('o', 'x')", "fxxbar");
        testStringMethod("'foobar'.replace('o', 'x')", "fxxbar");
        testStringMethod("'foobar'.replaceFirst('o', 'x')", "fxobar");
        testStringMethod("'foobar'.toUpperCase()", "FOOBAR");
        testStringMethod("'FOOBAR'.toLowerCase()", "foobar");

        // formatting, C-Style
        testStringMethod("'Hello %s!'.format('world')", "Hello world!");

        // Formatting, JSP style
        // Expand a template from local variables
        testStringMethod("""
                a = 1;
                b = 2;
                c = "foobar";
                return "${a} <%= b %> ${c}".expand();
                """, "1 2 foobar");

        // Override one local variable
        testStringMethod("""
                a = 1;
                b = 2;
                c = "foobar";
                return "${a} <%= b %> ${c}".expand({a: 3});
                """, "3 2 foobar");

        testStringMethod("""
                parts = "a\\nb\\nc".splitLines();
                assert(parts.size() == 3);
                assert(parts[0] == "a");
                assert(parts[1] == "b");
                assert(parts[2] == "c");
                return "ok";
                """, "ok");

        // number conversions
        testStringMethod("'foobar'.isNumeric()", false);
        testStringMethod("'123'.int()", 123L);
        testStringMethod("'123'.toInt()", 123L);
        testStringMethod("'123.45'.toFloat()", 123.45);
        testStringMethod("'123'.isNumeric()", true);
        testStringMethod("'123'.startsWithNumbers()", true);
        testStringMethod("'foobar'.startsWithNumbers()", false);

        // date / time parsing
        testStringMethod("'2020-12-31'.parseDate('yyyy-MM-dd')", LocalDate.of(2020, 12, 31));
        testStringMethod("'2020-12-31-23:59:59'.parseDateTime('yyyy-MM-dd-HH:mm:ss')", LocalDateTime.of(2020, 12, 31, 23, 59, 59));

        // common encodings
        testStringMethod("'Hello, world!'.urlEncode()", "Hello%2C+world%21");
        testStringMethod("'Hello%2C%20world%21'.urlDecode()", "Hello, world!");
        testStringMethod("'Hello%2C+world%21'.urlDecode()", "Hello, world!");
        testStringMethod("'SGVsbG8sIHdvcmxkIQ=='.decodeBase64()", "Hello, world!".getBytes(StandardCharsets.UTF_8));

        // special interest
        testStringMethod("'000123'.removeLeadingZeros()", "123");
        testStringMethod("'foobar'.toBinaryUtf8()", "foobar".getBytes(StandardCharsets.UTF_8));

    }

}
