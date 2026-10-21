// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.dispatch.HasDispatcher;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Too many arguments are an error, as in Python and Java: for script functions and lambdas (unless they collect more with
 * *rest or **options), and for every method of the built-in types.
 */
public class TooManyArgumentsTestCase extends JanitorTest {

    /** A sample value of each built-in type, as a script expression. */
    private static final Map<String, String> SAMPLES = Map.ofEntries(
            Map.entry("string", "\"abc\""),
            Map.entry("int", "1"),
            Map.entry("float", "1.5"),
            Map.entry("list", "[1, 2]"),
            Map.entry("map", "{a: 1}"),
            Map.entry("set", "[1].toSet()"),
            Map.entry("binary", "\"abc\".toBinaryUtf8()"),
            Map.entry("regex", "re/(a)/"),
            Map.entry("matcher", "re/(a)/.matcher(\"a\")"),
            Map.entry("duration", "@1d"),
            Map.entry("date", "@2024-01-01"),
            Map.entry("datetime", "@2024-01-01-10:00"));

    /** Methods that take any number of arguments on purpose. */
    private static final Set<String> VARIADIC = Set.of("string.format");

    private static JanitorObject run(final String source) throws JanitorCompilerException, JanitorRuntimeException {
        return OutputCatchingTestRuntime.fresh().compile("test", source).run();
    }

    private static Dispatcher<?> dispatcherOf(final JanitorObject object) {
        if (object instanceof HasDispatcher<?> hasDispatcher) {
            return hasDispatcher.getDispatcher();
        }
        return ((JanitorWrapper<?>) object).getDispatcher();
    }

    @Test
    void everyBuiltinMethodRejectsTooManyArguments() throws Exception {
        final List<String> lenient = new ArrayList<>();
        int checked = 0;
        for (final Map.Entry<String, String> sample : SAMPLES.entrySet()) {
            final Dispatcher<?> dispatcher = dispatcherOf(run("return " + sample.getValue() + ";"));
            final List<String> methods = dispatcher.streamAttributeNames()
                    .filter(name -> !name.startsWith("__"))
                    .filter(name -> dispatcher.getMetaData(name, Janitor.MetaData.TYPE_HINT) == Janitor.MetaData.TypeHint.METHOD)
                    .distinct().toList();
            for (final String method : methods) {
                final String id = sample.getKey() + "." + method;
                if (VARIADIC.contains(id)) {
                    continue;
                }
                checked++;
                final String source = "x = " + sample.getValue() + "; x." + method + "(1, 2, 3, 4, 5);";
                try {
                    run(source);
                    lenient.add(id + " (no error)");
                } catch (JanitorArgumentException e) {
                    if (!e.getText().contains("argument")) {
                        lenient.add(id + " (" + e.getText() + ")");
                    }
                } catch (JanitorRuntimeException e) {
                    lenient.add(id + " (" + e.getClass().getSimpleName() + ": " + e.getText() + ")");
                }
            }
        }
        assertTrue(checked > 90, "expected to check all built-in methods, but checked only " + checked);
        assertEquals(List.of(), lenient, "built-in methods must reject too many arguments with an argument error");
    }

    @Test
    void scriptFunctions() throws Exception {
        assertThrows(JanitorArgumentException.class, () -> run("function f(a) { return a; } f(1, 2);"));
        assertThrows(JanitorArgumentException.class, () -> run("function f() { return 1; } f(1);"));
        assertThrows(JanitorArgumentException.class, () -> run("function f(a, b = 2) { return a + b; } f(1, 2, 3);"));
        assertThrows(JanitorArgumentException.class, () -> run("f = x -> x; f(1, 2);"));
        assertThrows(JanitorArgumentException.class, () -> run("[1, 2].map((a, b) -> a);"));
        assertEquals(3L, run("function f(a, b = 2) { return a + b; } f(1)").janitorGetHostValue());
        assertEquals(4L, run("function f(a, b = 2) { return a + b; } f(1, 3)").janitorGetHostValue());
    }

    @Test
    void restAndOptionsCollectMore() throws Exception {
        assertEquals("[2, 3]", run("function f(a, *rest) { return rest; } f(1, 2, 3)").janitorToString());
        assertEquals("1", run("function f(a, **options) { return a; } f(1, x = 2)").janitorToString());
    }

    @Test
    void theMessageNamesTheLimit() {
        final JanitorArgumentException e = assertThrows(JanitorArgumentException.class, () -> run("\"banana\".indexOf(\"a\", 2);"));
        assertTrue(e.getText().contains("indexOf"), e.getText());
        assertTrue(e.getText().contains("exactly 1"), e.getText());
    }
}
