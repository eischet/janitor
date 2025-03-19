package com.eischet.janitor;

import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JBool;
import com.eischet.janitor.api.types.builtin.JInt;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.builtin.JString;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test common string operations and methods.
 */
public class JStringTestCase {

    private void testStringMethod(@Language("Janitor") final String script, final Object expectedResult) throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        final RunnableScript runnableScript = rt.compile("test", script);
        final @NotNull JanitorObject result = runnableScript.run();
        final Object actualResult = result.janitorGetHostValue();
        assertEquals(expectedResult, actualResult, script);
    }

    private void testStringMethod(final String script, final byte[] expectedResult) throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
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
    void basics() throws JanitorCompilerException, JanitorRuntimeException {
        OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        @NotNull JString string = rt.getEnvironment().getBuiltinTypes().string("foobar");
        assertEquals("foobar", string.janitorGetHostValue());
        assertEquals("foobar", string.janitorToString());
        assertEquals("foobar", string.toString());

        @NotNull JMap map = rt.getEnvironment().getBuiltinTypes().map();
        @NotNull JInt oneTo = rt.getEnvironment().getBuiltinTypes().integer(1);
        map.put(string, oneTo);
        JanitorObject oneFro = map.get(string);
        assertEquals(oneFro, oneTo);
        assertSame(oneFro, oneTo);

        assertEquals(1, map.keySet().size());
        assertSame(string, map.keySet().stream().findFirst().orElse(null));

        @NotNull JanitorObject map2raw = rt.compile("newmap", "{'foobar':1}").run();
        assertInstanceOf(JMap.class, map2raw);
        JMap map2 = (JMap) map2raw;

        assertFalse(map2.isEmpty());
        assertFalse(map2.keySet().isEmpty());
        assertFalse(map2.janitorGetHostValue().isEmpty());

        Map<JanitorObject, JanitorObject> unpacked = map2.janitorGetHostValue();
        System.out.println(unpacked);

        JanitorObject mapGet = unpacked.get(string);
        System.out.println(mapGet);


        assertEquals(map.get(string), map2.get(string));


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

        // some operations, not actual methods
        testStringMethod("'foobar'[1:-1]", "ooba");
        testStringMethod("'foobar'[1:]", "oobar");
        testStringMethod("'foobar'[:-1]", "fooba");
        testStringMethod("'foobar'[1:1]", "");
        testStringMethod("'foobar'[1:2]", "o");
        testStringMethod("'foobar'[1:3]", "oo");
        testStringMethod("'foobar'[1:4]", "oob");
        testStringMethod("'foobar'[1:5]", "ooba");
        testStringMethod("'foobar'[1:6]", "oobar");
        testStringMethod("'foobar'[-6]", "f");
        testStringMethod("'foobar'[-5]", "o");
        testStringMethod("'foobar'[-1]", "r");

        // TODO: this returns f at the moment, but "" is better -> testStringMethod("'foobar'[1:0]", "");
        // TODO: throws index out of bounds at the moment, but should not! testStringMethod("'foobar'[1:7]", "oobar");
        // same: testStringMethod("'foobar'[1:8]", "oobar");
        // Check how Python does it (and these snippets should be valid python code, too, anyways!). Do it like Python.

    }

    @Test
    public void wildcards() throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        assertEquals(Boolean.TRUE, rt.compile("test", "'foo' ~ 'f*'").run().janitorGetHostValue());
        assertEquals(Boolean.FALSE, rt.compile("test", "'bar' ~ 'f*'").run().janitorGetHostValue());
    }

    @Test
    public void regexes() throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        assertEquals(Boolean.TRUE, rt.compile("test", "'foo' ~ re/foo/").run().janitorGetHostValue());
        assertEquals(Boolean.TRUE, rt.compile("test", "'foo' ~ re/f../").run().janitorGetHostValue());
        assertEquals(Boolean.FALSE, rt.compile("test", "'foo' ~ re/\\d+/").run().janitorGetHostValue());
        assertEquals(Boolean.TRUE, rt.compile("test", "'12345' ~ re/\\d+/").run().janitorGetHostValue());
        assertEquals(Boolean.FALSE, rt.compile("test", "'12345' !~ re/\\d+/").run().janitorGetHostValue());
        assertEquals("245005", rt.compile("test", "re/[^1-9]+([0-9]+).*/.extract('SW245005-POS04')").run().janitorGetHostValue());
        assertEquals(JBool.TRUE, rt.compile("test", " 'foo/bar' ~ re/foo.bar/").run());
        assertEquals(JBool.FALSE, rt.compile("test", " 'foo/bar' !~ re/foo.bar/").run());
        assertEquals(JBool.TRUE, rt.compile("test", " 'foo/bar' ~ re/foo\\/bar/").run());
        assertEquals(JBool.FALSE, rt.compile("test", " 'foo/bar' !~ re/foo\\/bar/").run());
    }

    @Test
    public void splitting() throws JanitorCompilerException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        Function<String, JanitorObject> str = it -> rt.getBuiltinTypes().nullableString(it);
        // Splitting by a string returns a list of strings, not including the separator string:
        assertEquals(List.of(str.apply("a"), str.apply("b"), str.apply("c")), rt.compile("test", "'a,b,c'.split(',')").run().janitorGetHostValue());
        assertEquals(List.of(str.apply("a"), str.apply("b")), rt.compile("test", "'a,b,'.split(',')").run().janitorGetHostValue());
        // Splitting by an empty string returns a list of characters:
        assertEquals(List.of(str.apply("a"), str.apply("b"), str.apply("c")), rt.compile("test", "'abc'.split('')").run().janitorGetHostValue());
        // Splitting an empty string returns an empty list:
        assertEquals(List.of(), rt.compile("test", "''.split('')").run().janitorGetHostValue());
        // Split by a regular expression:
        assertEquals(List.of(str.apply("a"), str.apply("b"), str.apply("c"), str.apply("d")), rt.compile("test", "'a1b2c3d'.split(re/\\d/)").run().janitorGetHostValue());
        // same as:
        assertEquals(List.of(str.apply("a"), str.apply("b"), str.apply("c"), str.apply("d")), rt.compile("test", "re/\\d/.split('a1b2c3d')").run().janitorGetHostValue());

        @NotNull final JanitorObject anomaly = rt.compile("test", "''.split('foobar')").run();
        System.err.println(anomaly);
        System.err.println(anomaly.getClass());
        System.err.println(anomaly.janitorIsTrue());

        // assertEquals(List.of(), .janitorGetHostValue());


        rt.compile("sameInScriptCodeOnly", """
                assert('a,b,c'.split(',') == ['a', 'b', 'c']);
                assert('a,b,'.split(',') == ['a', 'b']);
                assert('abc'.split('') == ['a', 'b', 'c']);
                assert(''.split('') == []);
                // assert(''.split('foobar').isEmpty());
                print("->", ''.split('foobar'), "<-");
                assert('a1b2c3d'.split(re/\\d/) == ['a', 'b', 'c', 'd']);
                assert(re/\\d/.split('a1b2c3d') == ['a', 'b', 'c', 'd']);
                """).run();
        System.err.println(rt.getAllOutput());


    }
    
    
    

}
