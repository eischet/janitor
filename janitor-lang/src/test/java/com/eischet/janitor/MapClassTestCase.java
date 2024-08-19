package com.eischet.janitor;

import com.eischet.janitor.api.types.BuiltinTypes;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.types.RuntimeConsumer;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import com.eischet.janitor.toolbox.json.api.JsonException;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the map class.
 */
public class MapClassTestCase {

    /**
     * Exercise some basic map operations
     *
     * @throws JsonException           on errors
     * @throws JanitorRuntimeException on errors
     */
    @Test
    public void mapTests() throws JsonException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();
        final @NotNull BuiltinTypes bt = rt.getEnvironment().getBuiltinTypes(); // alias builtin types

        final JMap empty = rt.getEnvironment().parseJsonToMap("{}");
        assertEquals(0, empty.size(), "empty maps have size 0");
        assertTrue(empty.isEmpty(), "empty maps are empty");
        assertTrue(empty.isDefaultOrEmpty(), "empty maps are default/empty");
        assertFalse(empty.janitorIsTrue(), "empty maps are false");

        assertEquals(empty, bt.map(), "empty maps should be equal");

        @Language("JSON") final String JSON_SOURCE = "{\"a\": 1, \"b\": 2, \"c\": 3}";

        final JMap map = rt.getEnvironment().parseJsonToMap(JSON_SOURCE);
        // I'd prefer having bt.integer(1..4), but that's not how JSON works.
        assertEquals(bt.floatingPoint(1.0), map.get(bt.string("a")));
        assertEquals(bt.floatingPoint(2.0), map.get(bt.string("b")));
        assertEquals(bt.floatingPoint(3.0), map.get(bt.string("c")));
        assertFalse(map.isEmpty(), "non-empty maps are not empty");
        assertFalse(map.isDefaultOrEmpty(), "non-empty maps are not default/empty");
        assertTrue(map.janitorIsTrue(), "non-empty maps are true");

        /*
         * simplify script execution for the rest of the test.
         */
        final RuntimeConsumer<String> play = (@Language("Janitor") var script) -> {
            try {
                final RunnableScript runnableScript = rt.compile("test", script);
                runnableScript.run(g -> g.bind("map", map).bind("empty", empty));
            } catch (JanitorCompilerException e) {
                throw new RuntimeException(e); // RuntimeConsumers allow Runtime Errors, but not compile errors, so let's just convert
            }
        };

        play.accept("assert(empty.size() == 0);");
        play.accept("assert(empty.isEmpty());");
        play.accept("assert(empty.keys() == [])");

        play.accept("assert(map.a == 1.0);");
        play.accept("assert(map.a.int == 1);"); // LATER: not really happy with this right now
        play.accept("assert(map.b == 2.0);");
        play.accept("assert(map['b'] == 2.0);");
        play.accept("assert(map.size() == 3);");

        play.accept("""
                keys = map.keys();
                assert(keys.size() == 3);
                assert(keys.contains('a'));
                assert(keys.contains('b'));
                assert(keys.contains('c'));
                assert(not keys.contains('d'));
                """);

        play.accept("""
                values = map.values();
                assert(values.size() == 3);
                assert(values.contains(1.0));
                assert(values.contains(2.0));
                assert(values.contains(3.0));
                assert(not values.contains(4.0));
                """);

        final JMap anotherMap = rt.getEnvironment().parseJsonToMap(JSON_SOURCE);
        assertEquals(map, anotherMap, "two maps parsed from the same JSON source should be equal");


        play.accept("map['d'] = 4.0;");
        assertEquals(map.get(bt.string("d")), bt.floatingPoint(4.0));
        assertEquals(4, map.size(), "size increases when adding elements");
        play.accept("map.put('e', 5.0);");
        assertEquals(map.get(bt.string("e")), bt.floatingPoint(5.0));
        assertEquals(5, map.size(), "size increases when adding elements");


    }

}
