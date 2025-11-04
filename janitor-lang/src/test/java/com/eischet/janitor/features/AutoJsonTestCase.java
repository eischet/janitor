package com.eischet.janitor.features;

import com.eischet.janitor.JanitorTest;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.builtin.JDate;
import com.eischet.janitor.api.types.builtin.JDateTime;
import com.eischet.janitor.api.types.builtin.JList;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.runtime.OutputCatchingTestRuntime;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonInputStream;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Working on automatic import/export of JSON from dispatch tables.
 */
public class AutoJsonTestCase extends JanitorTest {

    private static final OutputCatchingTestRuntime rt = OutputCatchingTestRuntime.fresh();


    private static class SimpleObject extends JanitorComposed<SimpleObject> {
        private static final DispatchTable<SimpleObject> DISPATCH = new DispatchTable<>(null);

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
            return DISPATCH.writeToJson(this);
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
                {"foo":"baz"}""";

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
        final Object shorterJson = SimpleObject.DISPATCH.writeToJson(testObject);
        assertEquals(SIMPLE_ONE, shorterJson);

        testObject.setBar("");
        assertEquals(SIMPLE_TWO, SimpleObject.DISPATCH.writeToJson(testObject));

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
        // makes no sense, really: assertEquals("", so2.getBar());

        final SimpleObject so3 = SimpleObject.fromJson(rt.getEnvironment().getLenientJsonConsumer(SIMPLE_THREE));
        assertEquals("baz", so3.getFoo());
        assertNull(so3.getBar());

    }

    private static class ThingWithListProp extends JanitorComposed<ThingWithListProp> {
        private static final DispatchTable<ThingWithListProp> DISPATCH = new DispatchTable<>(null);
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

        final String json = ThingWithListProp.DISPATCH.writeToJson(thing);
        assertEquals(LIST_JSON, json);

        ThingWithListProp otherThing = ThingWithListProp.DISPATCH.readFromJson(ThingWithListProp::new, LIST_JSON);
        assertEquals(thing.getList(), otherThing.getList());



    }


    private static class Mixed extends JanitorComposed<Mixed> {
        private static final DispatchTable<Mixed> DISPATCH = new DispatchTable<>(null);

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

        assertEquals(MY_UGLY_LIST, mixer.toJson());


        final Mixed read = Mixed.DISPATCH.readFromJson(Mixed::new, MY_UGLY_LIST);
        assertNull(read.getB().getBar());
        assertEquals(4, read.getA().getList().size());
        assertEquals("baz", read.getB().getFoo());
        assertEquals(List.of("a", "b", "c", "d"), read.getA().getList());

    }


    private static class Inheritor extends Mixed {
        private static final DispatchTable<Inheritor> DISPATCH = new DispatchTable<>(null);
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


    @Test void simpleList() throws JsonException {
        @Language("JSON") final String JSON = "[1, 2, 3, 4, 5]";
        final JList list = Janitor.list();
        list.readJson(Janitor.current().getLenientJsonConsumer(JSON));
        assertEquals(5, list.size());
    }

    @Test void dateOutput() throws JsonException {
        @NotNull final JDate fifty = Janitor.date(LocalDate.of(2026, 1, 10));
        final String jsonForm = Janitor.current().writeJson(fifty);
        assertEquals("\"2026-01-10\"", jsonForm);

        final JList list = Janitor.list();
        list.add(fifty);
        @Language("JSON") final String jsonListForm = Janitor.current().writeJson(list);
        assertEquals("[\"2026-01-10\"]", jsonListForm);

        @NotNull final JanitorObject parsed = Janitor.nullableDateFromJsonString("2026-01-10");
        assertEquals(fifty, parsed);


        final JList readingList = Janitor.list();
        readingList.readJson(Janitor.current().getLenientJsonConsumer(jsonListForm));
        assertEquals(1, readingList.size());
        // cannot work: JSON does not have a Data type, so we must expect a string... assertEquals(fifty, readingList.get(0));
        assertEquals("2026-01-10", readingList.get(0).toString());
    }

    @Test void dateTimeOutput() throws JsonException {
        @NotNull final JDateTime fifty = Janitor.dateTime(LocalDateTime.of(2026, 1, 10, 12, 30, 45));
        final String jsonForm = Janitor.current().writeJson(fifty);
        assertEquals("\"2026-01-10T12:30:45\"", jsonForm);

        @NotNull final JanitorObject parsed = Janitor.nullableDateTimeFromJsonString("2026-01-10T12:30:45");
        assertEquals(fifty, parsed);


        final JList list = Janitor.list();
        list.add(fifty);
        @Language("JSON") final String jsonListForm = Janitor.current().writeJson(list);
        assertEquals("[\"2026-01-10T12:30:45\"]", jsonListForm);

        final JList readingList = Janitor.list();
        readingList.readJson(Janitor.current().getLenientJsonConsumer(jsonListForm));
        assertEquals(1, readingList.size());
        // cannot work: JSON does not have a Data type, so we must expect a string... assertEquals(fifty, readingList.get(0));
        assertEquals("2026-01-10T12:30:45", readingList.get(0).toString());
    }

    private static class Person extends JanitorComposed<Person> {
        private static final DispatchTable<Person> DISPATCH = new DispatchTable<>();
        static {
            DISPATCH.addDateProperty("birthday", Person::getBirthday, Person::setBirthday);
            DISPATCH.addDateTimeProperty("nextAppointment", Person::getNextAppointment, Person::setNextAppointment);
        }

        public Person() {
            super(DISPATCH);
        }

        private LocalDate birthday;
        private LocalDateTime nextAppointment;

        public LocalDate getBirthday() {
            return birthday;
        }

        public void setBirthday(final LocalDate birthday) {
            this.birthday = birthday;
        }

        public LocalDateTime getNextAppointment() {
            return nextAppointment;
        }

        public void setNextAppointment(final LocalDateTime nextAppointment) {
            this.nextAppointment = nextAppointment;
        }
    }

    /**
     * Make sure that Dates and DateTimes can be written to and read from JSON, by dispatch tables.
     * @throws JsonException on errors
     */
    @Test void dateTimeOutputWithinAComposedObject() throws JsonException {
        final Person stefan = new Person();
        stefan.setBirthday(LocalDate.of(2026, 1, 10));
        stefan.setNextAppointment(LocalDateTime.of(2026, 5, 11, 12, 30, 45));
        @Language("JSON") final String jsonForm = Janitor.current().writeJson(stefan);
        assertEquals("{\"birthday\":\"2026-01-10\",\"nextAppointment\":\"2026-05-11T12:30:45\"}", jsonForm);

        final Person clone = new Person();
        clone.readJson(Janitor.current().getLenientJsonConsumer(jsonForm));
        assertEquals(stefan.birthday, clone.birthday);
        assertEquals(stefan.nextAppointment, clone.nextAppointment);
    }



}
