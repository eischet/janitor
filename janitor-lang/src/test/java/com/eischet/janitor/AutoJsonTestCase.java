package com.eischet.janitor;

import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Working on automatic import/export of JSON from dispatch tables.
 */
public class AutoJsonTestCase {

    private static final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();


    private static class SimpleObject extends JanitorComposed<SimpleObject> {
        private static final DispatchTable<SimpleObject> DISPATCH = new DispatchTable<>();

        static {
            DISPATCH.addStringProperty("foo", SimpleObject::getFoo, SimpleObject::setFoo);
            DISPATCH.addStringProperty("bar", SimpleObject::getBar, SimpleObject::setBar);
        }

        public SimpleObject() {
            super(DISPATCH);
        }

        private String foo;
        private String bar;

        public String getFoo() {
            return foo;
        }

        public void setFoo(final String foo) {
            this.foo = foo;
        }

        public String getBar() {
            return bar;
        }

        public void setBar(final String bar) {
            this.bar = bar;
        }

        public String toJson() throws JsonException {
            return DISPATCH.writeToJson(rt.getEnvironment(), this);
        }

        public static SimpleObject fromJson(final JsonInputStream stream) throws JsonException {
            return DISPATCH.readFromJson(SimpleObject::new, stream);
        }
    }

    @Language("JSON")
    private static final String SIMPLE_ONE = """
                {"foo":"baz","bar":"frobnicate"}""";

    @Language("JSON")
    private static final String SIMPLE_TWO = """
                {"foo":"baz","bar":""}""";

    @Language("JSON")
    private static final String SIMPLE_THREE = """
                {"foo":"baz"}""";

    /**
     * We can now create JSON automatically from objects that have dispatch tables.
     * TODO: add more complex tests, requiring code in GenericDispatchTable.java that's not yet there, e.g. List attributes aren't implemented yet.
     * @throws JsonException on errors
     */
    @Test
    public void writeSimpleObject() throws JsonException {
        final SimpleObject testObject = new SimpleObject();
        testObject.setFoo("baz");
        testObject.setBar("frobnicate");

        final String json = rt.getEnvironment().writeJson(producer -> SimpleObject.DISPATCH.writeToJson(producer, testObject));


        assertEquals(SIMPLE_ONE, json);

        // shorthand:
        final Object shorterJson = SimpleObject.DISPATCH.writeToJson(rt.getEnvironment(), testObject);
        assertEquals(SIMPLE_ONE, shorterJson);

        testObject.setBar("");
        assertEquals(SIMPLE_TWO, SimpleObject.DISPATCH.writeToJson(rt.getEnvironment(), testObject));

        // event shorter
        assertEquals(SIMPLE_TWO, testObject.toJson());

        testObject.setBar(null);
        assertEquals(SIMPLE_THREE, testObject.toJson());

        // By now, we have achieved fully automatic conversions from Janitor objects to JSON, without any form of runtime reflection.
        // I really like this. If someone wrote e.g. an annotation processor to auto-generate dispatch tables, this would be fully
        // automatic and still not use any kind of reflection at runtime.
        // Right now, of course, not all properties are fully implemented for reading/writing JSON, so the generated JSON strings will contain only
        // a subset of fields right now.


    }

    /**
     * We can read JSON, too.
     * TODO: add more complex tests, requiring code in GenericDispatchTable.java that's not yet there
     * @throws JsonException on errors
     */
    @Test
    public void readSimpleObject() throws JsonException {
        final SimpleObject so = SimpleObject.DISPATCH.readFromJson(SimpleObject::new, rt.getEnvironment().getLenientJsonConsumer(SIMPLE_ONE));
        assertEquals("baz", so.getFoo());
        assertEquals("frobnicate", so.getBar());

        final SimpleObject so2 = SimpleObject.fromJson(rt.getEnvironment().getLenientJsonConsumer(SIMPLE_TWO));
        assertEquals("baz", so2.getFoo());
        assertEquals("", so2.getBar());

        final SimpleObject so3 = SimpleObject.fromJson(rt.getEnvironment().getLenientJsonConsumer(SIMPLE_THREE));
        assertEquals("baz", so3.getFoo());
        assertNull(so3.getBar());

    }

    private static class ThingWithListProp extends JanitorComposed<ThingWithListProp> {
        private static final DispatchTable<ThingWithListProp> DISPATCH = new DispatchTable<>();
        static {
            DISPATCH.addListOfStringsProperty("list", ThingWithListProp::getList, ThingWithListProp::setList);
        }
        private List<String> list;

        public ThingWithListProp() {
            super(DISPATCH);
        }

        public List<String> getList() {
            return list;
        }

        public void setList(final List<String> list) {
            this.list = list;
        }
    }

    @Language("JSON")
    private static final String LIST_JSON = "{\"list\":[\"foo\",\"bar\",\"baz\"]}";

    @Test
    public void testListProp() throws JsonException {

        final ThingWithListProp thing = new ThingWithListProp();
        thing.setList(List.of("foo", "bar", "baz"));

        final String json = ThingWithListProp.DISPATCH.writeToJson(rt.getEnvironment(), thing);
        assertEquals(LIST_JSON, json);

        ThingWithListProp otherThing = ThingWithListProp.DISPATCH.readFromJson(rt.getEnvironment(), ThingWithListProp::new, LIST_JSON);
        assertEquals(thing.getList(), otherThing.getList());



    }


    private static class Mixed extends JanitorComposed<Mixed> {
        private static final DispatchTable<Mixed> DISPATCH = new DispatchTable<>();

        static {
            DISPATCH.addObjectProperty("a", Mixed::getA, Mixed::setA, ThingWithListProp::new);
            DISPATCH.addObjectProperty("b", Mixed::getB, Mixed::setB, SimpleObject::new);
        }


        private ThingWithListProp a;
        private SimpleObject b;

        public Mixed() {
            super(DISPATCH);
        }

        public Mixed(final Dispatcher<Mixed> dispatcher) {
            super(dispatcher);
        }

        public ThingWithListProp getA() {
            return a;
        }

        public void setA(final ThingWithListProp a) {
            this.a = a;
        }

        public SimpleObject getB() {
            return b;
        }

        public void setB(final SimpleObject b) {
            this.b = b;
        }
    }


    @Test
    public void testMixedClass() throws JsonException {
        @Language("JSON") final String MY_UGLY_LIST = "{\"a\":{\"list\":[\"a\",\"b\",\"c\",\"d\"]},\"b\":{\"foo\":\"baz\"}}";

        final Mixed mixer = new Mixed();
        mixer.setA(new ThingWithListProp());
        mixer.setB(new SimpleObject());
        mixer.getA().setList(List.of("a", "b", "c", "d"));
        mixer.getB().setFoo("baz");

        assertEquals(MY_UGLY_LIST, mixer.toJson(rt.getEnvironment()));


        final Mixed read = Mixed.DISPATCH.readFromJson(rt.getEnvironment(), Mixed::new, MY_UGLY_LIST);
        assertNull(read.getB().getBar());
        assertEquals(4, read.getA().getList().size());
        assertEquals("baz", read.getB().getFoo());
        assertEquals(List.of("a", "b", "c", "d"), read.getA().getList());

    }


    private static class Inheritor extends Mixed {
        private static final DispatchTable<Inheritor> DISPATCH = new DispatchTable<>();
        static {
            DISPATCH.addStringProperty("gumbo", Inheritor::getGumbo, Inheritor::setGumbo);
        }

        public Inheritor() {
            super(Dispatcher.inherit(Mixed.DISPATCH, DISPATCH));
        }

        private String gumbo;

        public String getGumbo() {
            return gumbo;
        }

        public void setGumbo(final String gumbo) {
            this.gumbo = gumbo;
        }
    }

    /* TODO: same test as above, but make it work for "subclasses"
    @Test
    public void testMixedClassInheritor() throws JsonException {
        @Language("JSON") final String MY_UGLY_LIST = "{\"a\":{\"list\":[\"a\",\"b\",\"c\",\"d\"]},\"b\":{\"foo\":\"baz\"}}";

        final Inheritor mixer = new Inheritor();
        mixer.setA(new ThingWithListProp());
        mixer.setB(new SimpleObject());
        mixer.getA().setList(List.of("a", "b", "c", "d"));
        mixer.getB().setFoo("baz");

        assertEquals(MY_UGLY_LIST, mixer.toJson(rt.getEnvironment()));


        final Inheritor read = Inheritor.DISPATCH.readFromJson(rt.getEnvironment(), Inheritor::new, MY_UGLY_LIST);
        assertNull(read.getB().getBar());
        assertEquals(4, read.getA().getList().size());
        assertEquals("baz", read.getB().getFoo());
        assertEquals(List.of("a", "b", "c", "d"), read.getA().getList());

    }
     */



}
