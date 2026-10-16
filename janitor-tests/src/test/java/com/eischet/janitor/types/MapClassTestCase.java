// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.types;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.TestEnv;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.RunnableScript;
import com.eischet.janitor.api.errors.compiler.JanitorCompilerException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.builtin.JMap;
import com.eischet.janitor.api.types.functions.JCallArgs;
import com.eischet.janitor.api.types.functions.JCallable;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import com.eischet.janitor.toolbox.json.api.JsonException;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the map class.
 */
public class MapClassTestCase extends JanitorTest {

    /**
     * Exercise some basic map operations
     *
     * @throws JsonException           on errors
     * @throws JanitorRuntimeException on errors
     */
    @Test
    public void mapTests() throws JsonException, JanitorRuntimeException {
        final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();

        final JMap empty = rt.getEnvironment().parseJsonToMap("{}");
        assertEquals(0, empty.size(), "empty maps have size 0");
        assertTrue(empty.isEmpty(), "empty maps are empty");
        assertTrue(empty.isDefaultOrEmpty(), "empty maps are default/empty");
        assertFalse(empty.janitorIsTrue(), "empty maps are false");

        assertEquals(empty, Janitor.map(), "empty maps should be equal");

        @Language("JSON") final String JSON_SOURCE = "{\"a\": 1, \"b\": 2, \"c\": 3}";

        final JMap map = rt.getEnvironment().parseJsonToMap(JSON_SOURCE);
        // I'd prefer having bt.integer(1..4), but that's not how JSON works.
        assertEquals(Janitor.integer(1), map.get(Janitor.string("a")));
        assertEquals(Janitor.integer(2), map.get(Janitor.string("b")));
        assertEquals(Janitor.integer(3), map.get(Janitor.string("c")));
        assertFalse(map.isEmpty(), "non-empty maps are not empty");
        assertFalse(map.isDefaultOrEmpty(), "non-empty maps are not default/empty");
        assertTrue(map.janitorIsTrue(), "non-empty maps are true");

        /*
         * simplify script execution for the rest of the test.
         */
        final TestEnv.ScriptConsumer play = (@Language("Janitor") var script) -> {
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
                assert(values.contains(1));
                assert(values.contains(2));
                assert(values.contains(3));
                assert(not values.contains(4));
                """);

        final JMap anotherMap = rt.getEnvironment().parseJsonToMap(JSON_SOURCE);
        assertEquals(map, anotherMap, "two maps parsed from the same JSON source should be equal");


        play.accept("map['d'] = 4.0;");
        assertEquals(map.get(Janitor.string("d")), Janitor.floatingPoint(4.0));
        assertEquals(4, map.size(), "size increases when adding elements");
        play.accept("map.put('e', 5.0);");
        assertEquals(map.get(Janitor.string("e")), Janitor.floatingPoint(5.0));
        assertEquals(5, map.size(), "size increases when adding elements");

        play.accept("map['foobar'] = map.d + 1;");
        assertEquals(6, map.size(), "size increases when adding elements");

        play.accept("map.foobar == 5.0;");

        play.accept("assert(map.missingKey == null);");

        // new copy method:
        play.accept("""
                m1 = {}
                m1["foo"] = "bar"
                m2 = m1.copy()
                assert(m1.foo == "bar")
                assert(m2.foo == "bar")
                m2["foo"] = "whoop"
                assert(m1.foo == "bar")
                assert(m2.foo == "whoop")
                """);

        // map.foo = "bar" as shorthand for map["foo"] = "bar":
        play.accept("""
                m1 = {}
                m1.foo = "bar"
                assert(m1["foo"] == "bar")
                assert(m1.foo == "bar")
                m1["foo"] = "baz";
                assert(m1["foo"] == "baz")
                assert(m1.foo == "baz")
                m1.foo = "bar"
                assert(m1["foo"] == "bar")
                assert(m1.foo == "bar")
                
                m1["isEmpty"] = 17;
                assert(not m1.isEmpty()); // still calls the method!
                assert(m1["isEmpty"] == 17); // retrieves the value
                """);


    }

    @Test void removeEntries() throws Exception {
        assertEquals("1\n{b=2}\n", getOutput("""
                m = {a: 1, b: 2};
                print(m.remove("a"));
                print(m);
                """));
        // removing a missing key returns null and leaves the map unchanged:
        assertEquals("null\n1\n", getOutput("""
                m = {a: 1};
                print(m.remove("x"));
                print(m.size());
                """));
        // the host map is modified, too:
        final JMap data = Janitor.map();
        data.put("foo", "bar");
        data.put("baz", 17);
        evaluate("assert(data.remove('foo') == 'bar')", g -> g.bind("data", data));
        assertEquals(1, data.size());
        assertFalse(data.janitorGetHostValue().containsKey(Janitor.string("foo")));
        // exactly one argument is required:
        assertThrows(JanitorRuntimeException.class, () -> evaluate("{a: 1}.remove()"));
        assertThrows(JanitorRuntimeException.class, () -> evaluate("{a: 1}.remove('a', 'b')"));
    }

    @Test void clearEntries() throws Exception {
        assertEquals("{}\n", getOutput("""
                m = {a: 1, b: 2};
                m.clear();
                print(m);
                """));
        final JMap data = Janitor.map();
        data.put("foo", "bar");
        evaluate("data.clear()", g -> g.bind("data", data));
        assertTrue(data.isEmpty());
    }

    @Test void containsKeyAndValue() throws Exception {
        evaluate("""
                m = {a: 1, b: null};
                assert(m.containsKey("a"));
                assert(m.containsKey("b")); // present, even though the value is null
                assert(not m.containsKey("c"));
                assert(m.containsValue(1));
                assert(not m.containsValue(2));
                """);
    }

    @Test void argumentCountsAreChecked() {
        assertThrows(JanitorRuntimeException.class, () -> evaluate("{}.put('a')"));
        assertThrows(JanitorRuntimeException.class, () -> evaluate("{}.put('a', 1, 2)"));
        assertThrows(JanitorRuntimeException.class, () -> evaluate("{}.size(1)"));
        assertThrows(JanitorRuntimeException.class, () -> evaluate("{}.isEmpty(1)"));
        assertThrows(JanitorRuntimeException.class, () -> evaluate("{}.keys(1)"));
        assertThrows(JanitorRuntimeException.class, () -> evaluate("{}.values(1)"));
        assertThrows(JanitorRuntimeException.class, () -> evaluate("{}.containsKey()"));
        assertThrows(JanitorRuntimeException.class, () -> evaluate("{}.containsValue()"));
        assertThrows(JanitorRuntimeException.class, () -> evaluate("{}.copy(1)"));
        assertThrows(JanitorRuntimeException.class, () -> evaluate("{}.clear(1)"));
    }

    @Test void hostValueIsTheWrappedMap() {
        final JMap data = Janitor.map();
        assertSame(data.janitorGetHostValue(), data.janitorGetHostValue(), "the wrapped map, not a copy");
        data.janitorGetHostValue().put(Janitor.string("foo"), Janitor.string("bar"));
        assertEquals(Janitor.string("bar"), data.get(Janitor.string("foo")));
    }

    @Test void valuesIsAnUnmodifiableCopy() {
        final JMap data = Janitor.map();
        data.put("foo", "bar");
        final var values = data.values();
        assertThrows(UnsupportedOperationException.class, values::clear);
        data.put("baz", 17);
        assertEquals(1, values.size(), "later changes to the map do not show up in the copy");
    }

    @Test void readJsonWithNullsAndNesting() throws Exception {
        @Language("JSON") final String json = "{\"a\": null, \"b\": 1, \"c\": [null, \"x\", {\"d\": true}], \"e\": {}}";
        final JMap map = Janitor.map();
        map.readJson(Janitor.current().getLenientJsonConsumer(json));
        assertEquals(4, map.size());
        assertTrue(map.containsKey(Janitor.string("a")));
        assertSame(Janitor.NULL, map.get(Janitor.string("a")));
        assertEquals(Janitor.numeric(1), map.get(Janitor.string("b")));
        final JList c = (JList) map.get(Janitor.string("c"));
        assertEquals(3, c.size());
        assertSame(Janitor.NULL, c.get(0));
        assertEquals(Janitor.string("x"), c.get(1));
        assertEquals(Janitor.TRUE, ((JMap) c.get(2)).get(Janitor.string("d")));
        assertTrue(((JMap) map.get(Janitor.string("e"))).isEmpty());

        // the Java and script entry points share the same implementation:
        assertEquals(map.exportToJson(), Janitor.parseJsonToMap(json).exportToJson()); // JList has no value equality, so compare the JSON
        evaluate("""
                m = {}.parseJson('{"a": null, "b": [1, null]}');
                assert(m.containsKey("a"));
                assert(m.a == null);
                assert(m.b.size() == 2);
                l = [].parseJson('[null, {"x": null}]');
                assert(l.size() == 2);
                assert(l[1].containsKey("x"));
                """);
    }

    @Test void readJsonCreatesNestedValuesInTheGivenEnvironment() throws Exception {
        final JanitorEnvironment envA = OutputCatchingTestRuntime.fresh().getEnvironment();
        final JanitorEnvironment envB = OutputCatchingTestRuntime.fresh().getEnvironment();
        final var mapDispatcherB = envB.getBuiltinTypes().map().getDispatcher();
        final var listDispatcherB = envB.getBuiltinTypes().list().getDispatcher();
        assertNotSame(envA.getBuiltinTypes().map().getDispatcher(), mapDispatcherB, "precondition: environments have their own builtin types");

        final JMap map = envB.getBuiltinTypes().map();
        map.readJson(envB, envB.getLenientJsonConsumer("{\"m\": {}, \"l\": [{}]}"));
        assertSame(mapDispatcherB, ((JMap) map.get(Janitor.string("m"))).getDispatcher());
        final JList list = (JList) map.get(Janitor.string("l"));
        assertSame(listDispatcherB, list.getDispatcher());
        assertSame(mapDispatcherB, ((JMap) list.get(0)).getDispatcher());
    }

    @Test void serializeTheEmptyMap() throws Exception {
        assertEquals("{}\n", getOutput("print({}.toJson())"));
    }

    @Test void serializingDefaultValuesInMapsIsNeeded() throws Exception {
        // This used to return {}, but that does not make sense for maps.
        assertEquals("{\"foo\":false}\n", getOutput("print({foo: false}.toJson())"));
    }

    @Test void callingFunctionsInMaps() throws Exception {
        final JCallable someDummyFunction = (process, arguments) -> Janitor.NULL;
        @NotNull final JMap data = Janitor.map();
        data.put("dummy", new JCallable.Wrapper(someDummyFunction, "dummy"));
        // Fails in 0.9.62:
        // NameException: member is not callable: dummy; on: {dummy=JCallable.Wrapper([function dummy])}[JMap] = TemporaryAssignable{name='dummy', value=Wrapper} [TemporaryAssignable]
        // While long, it's to the point: the map now returns a TemporaryAssignable to enable shorthand assignment to map properties. This, however, is not callable, so the unprepared
        // MemberCallExpression has to fail.
        evaluate("data.dummy()", g -> g.bind("data", data));



    }

}
