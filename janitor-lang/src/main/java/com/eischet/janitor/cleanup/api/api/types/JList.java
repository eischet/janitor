package com.eischet.janitor.cleanup.api.api.types;

import com.eischet.janitor.api.json.JsonInputStream;
import com.eischet.janitor.api.json.JsonException;
import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.cleanup.json.*;
import com.eischet.janitor.cleanup.runtime.types.*;
import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.primitive.IntSets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JList implements JanitorObject, JIterable, Iterable<JanitorObject>, JsonExportableList {

    private final MutableList<JanitorObject> list = Lists.mutable.empty();
    private final ImmutableMap<String, JNativeMethod> methods;


    public JList parseJson(final String json) throws JsonException {
        if (json == null || json.isBlank()) {
            return this;
        }
        final JsonInputStream reader = GsonInputStream.lenient(json);
        return parseJson(reader);
    }

    public JList parseJson(final JsonInputStream reader) throws JsonException {
        reader.beginArray();
        while (reader.hasNext()) {
            add(JCollection.parseJsonValue(reader));
        }
        reader.endArray();
        return this;
    }

    public JList() {
        final MutableMap<String, JNativeMethod> methods = Maps.mutable.empty();
        methods.put("parseJson", JNativeMethod.of(arguments -> parseJson(arguments.require(1).getString(0).janitorGetHostValue())));
        methods.put("__get__", JNativeMethod.of(arguments -> {
            if (arguments.size() == 1) {
                return getIndexed(arguments.require(1).getInt(0));
            }
            // LATER: make ranges assignable, too?
            if (arguments.size() == 2) {
                if (arguments.get(0) == JNull.NULL && arguments.get(1) == JNull.NULL) {
                    return getRange(JInt.of(0), JInt.of(list.size()));
                } else if (arguments.get(0) == JNull.NULL) {
                    return getRange(JInt.of(0), arguments.getInt(1));
                } else if (arguments.get(1) == JNull.NULL) {
                    return getRange(arguments.getInt(0), JInt.of(list.size()));
                }
                return getRange(arguments.getInt(0), arguments.getInt(1));
            }
            /*
            if (arguments.size() == 3) {

            }
             */
            throw new IndexOutOfBoundsException("invalid arguments for get: " + arguments);
            // throw new JanitorArgumentException(runningScript, "invalid arguments: " + arguments);
        }));
        methods.put("get", JNativeMethod.of(arguments -> {
            if (arguments.size() == 1) {
                return get(arguments.require(1).getInt(0));
            }
            if (arguments.size() == 2) {
                if (arguments.get(0) == JNull.NULL && arguments.get(1) == JNull.NULL) {
                    return getRange(JInt.of(0), JInt.of(list.size()));
                } else if (arguments.get(0) == JNull.NULL) {
                    return getRange(JInt.of(0), arguments.getInt(1));
                } else if (arguments.get(1) == JNull.NULL) {
                    return getRange(arguments.getInt(0), JInt.of(list.size()));
                }
                return getRange(arguments.getInt(0), arguments.getInt(1));
            }
            /*
            if (arguments.size() == 3) {

            }
             */
            throw new IndexOutOfBoundsException("invalid arguments for get: " + arguments);
            // throw new JanitorArgumentException(runningScript, "invalid arguments: " + arguments);
        } ));
        methods.put("put", JNativeMethod.ofVoid(arguments -> put(arguments.require(2).getInt(0), arguments.get(1))));
        methods.put("addAll", JNativeMethod.ofVoid(arguments -> {
            for (final JanitorObject jObj : arguments.getRequired(0, JList.class)) {
                add(jObj);
            }
        }));
        methods.put("removeAll", JNativeMethod.ofVoid(arguments -> {
            for (final JanitorObject jObj : arguments.getRequired(0, JList.class)) {
                remove(jObj);
            }
        }));
        methods.put("add", JNativeMethod.ofVoid(arguments -> {
            arguments.require(1, 2);
            if (arguments.size() == 1) {
                add(arguments.get(0));
            } else {
                add(arguments.getInt(0), arguments.get(1));
            }
        }));
        // methods.put("add", CSNativeMethod.ofVoid(arguments -> add(arguments.require(2).getInt(0), arguments.get(1))));
        methods.put("size", JNativeMethod.of(arguments -> {
            arguments.require(0);
            return JInt.of(list.size());
        }));
        methods.put("isEmpty", JNativeMethod.of(arguments -> {
            arguments.require(0);
            return JBool.map(list.isEmpty());
        }));
        methods.put("contains", JNativeMethod.of(arguments -> {
            final JanitorObject countable = arguments.require(1).get(0);
            for (final JanitorObject csObj : list) {
                if (Objects.equals(countable, csObj)) {
                    return JBool.TRUE;
                }
            }
            return JBool.FALSE;
        }));

        methods.put("toSet", JNativeMethod.of(arguments -> {
            arguments.require(0);
            return new JSet(list.stream());
        }));

        methods.put("randomSublist", JNativeMethod.of(arguments -> {
            final int count = arguments.getInt(0).getAsInt();
            if (count >= size()) {
                return new JList(this.list);
            }
            final Random random = new Random();
            final MutableIntSet indexes = IntSets.mutable.empty();
            while (indexes.size() < count) {
                indexes.add(random.nextInt(list.size()));
            }
            final MutableList<JanitorObject> result = Lists.mutable.empty();
            indexes.forEach(i -> result.add(list.get(i)));
            return new JList(result);
        }));

        methods.put("join", JNativeMethod.of(arguments -> {
            final String separator = arguments.getOptionalStringValue(0, " ");
            return JString.of(list.stream().map(JanitorObject::janitorToString).collect(Collectors.joining(separator)));
        }));

        methods.put("map", new JNativeMethod(new JNativeMethod.NativeCall() {
            @Override
            public JanitorObject execute(final JanitorScriptProcess runningScript, final JCallArgs arguments) throws Exception {
                final JanitorObject callable = arguments.getRequired(0, JanitorObject.class);
                if (callable instanceof JCallable func) {
                    JList result = new JList();
                    for (final JanitorObject e : list) {
                        result.add(func.call(runningScript, new JCallArgs("map", runningScript, Lists.immutable.of(e))));
                    }
                    return result;
                } else {
                    throw new JanitorArgumentException(runningScript, "invalid list::map parameter: " + callable);
                }
            }
        }));
        methods.put("filter", new JNativeMethod(new JNativeMethod.NativeCall() {
            @Override
            public JanitorObject execute(final JanitorScriptProcess runningScript, final JCallArgs arguments) throws Exception {
                final JanitorObject callable = arguments.getRequired(0, JanitorObject.class);
                if (callable instanceof JCallable func) {
                    JList result = new JList();
                    for (final JanitorObject e : list) {
                        if (func.call(runningScript, new JCallArgs("filter", runningScript, Lists.immutable.of(e))).janitorIsTrue()) {
                            result.add(e);
                        }
                    }
                    return result;
                } else {
                    throw new JanitorArgumentException(runningScript, "invalid list::filter parameter: " + callable);
                }
            }
        }));
        methods.put("count", JNativeMethod.of(arguments -> {
            final JanitorObject countable = arguments.require(1).get(0);
            int count = 0;
            for (final JanitorObject csObj : list) {
                if (Objects.equals(countable, csObj)) {
                    ++count;
                }
            }
            return JInt.of(count);
        }));
        methods.put("toJson", JNativeMethod.of(arguments -> {
            arguments.require(0);
            return JString.of(exportToJson());
        }));
        this.methods = methods.toImmutable();


    }

    @Override
    public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
        final JNativeMethod localMethod = methods.get(name);
        if (localMethod != null) {
            return localMethod;
        }
        return JanitorObject.super.janitorGetAttribute(runningScript, name, required);
    }

    public int size() {
        return list.size();
    }

    public JanitorObject get(int index) {
        return list.get(index);
    }

    public static int toIndex(final int index, final int len) {
        if (index >= 0) {
            return index;
        } else {
            return len + index; // Pythonic: -1 is the last element, etc.; +i because i is negative :o)
        }
    }


    public JanitorObject get(JInt index) {
        return list.get(toIndex(index.getAsInt(), list.size()));
    }

    public JanitorObject getIndexed(JInt index) {
        return new TemporaryAssignable(get(index), value -> list.set(toIndex(index.getAsInt(), list.size()), value));
    }

    public JanitorObject getRange(JInt start, JInt end) {
        // LATER: stepping
        final int startIndex = toIndex(start.getAsInt(), list.size());
        final int endIndex = toIndex(end.getAsInt(), list.size());
        final MutableList<JanitorObject> subList = list.subList(Math.min(startIndex, endIndex), Math.max(startIndex, endIndex));
        if (endIndex<startIndex) {
            subList.reverseThis();
        }
        return new JList(subList);
    }

    public void add(JInt i, JanitorObject value) {
        list.add(i.janitorGetHostValue().intValue(), value);
    }

    public void add(JanitorObject value) {
        list.add(value.janitorUnpack());
    }

    public void remove(JanitorObject value) {
        list.remove(value.janitorUnpack());
    }

    public void put(JInt index, JanitorObject value) {
        list.set(index.janitorGetHostValue().intValue(), value);
    }

    public JList(final ImmutableCollection<? extends JanitorObject> init) {
        this();
        list.addAllIterable(init);
    }

    public JList(final Stream<? extends JanitorObject> init) {
        this();
        init.forEach(list::add);
    }

    public JList(final MutableList<JanitorObject> elements) {
        this();
        list.addAll(elements);
    }

    public static JList of(final ImmutableList<? extends JanitorObject> valueList) {
        return new JList(valueList);
    }

    public static JList of(final Stream<? extends JanitorObject> valueStream) {
        return new JList(valueStream);
    }

    @Override
    public MutableList<JanitorObject> janitorGetHostValue() {
        return list;
    }

    @Override
    public String toString() {
        return list.toString();
    }

    @Override
    public boolean janitorIsTrue() {
        return !list.isEmpty();
    }

    @Override
    public Iterator<JanitorObject> getIterator() {
        return list.iterator();
    }

    public Stream<JanitorObject> stream() {
        return list.stream();
    }

    public JanitorObject popFirst() {
        if (list.isEmpty()) {
            return JNull.NULL;
        } else {
            return list.remove(0);
        }
    }

    @NotNull
    @Override
    public Iterator<JanitorObject> iterator() {
        return getIterator();
    }

    @Override
    public @NotNull String janitorClassName() {
        return "list";
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean isDefaultOrEmpty() {
        return isEmpty();
    }

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.beginArray();
        for (final JanitorObject jObj : list) {
            if (jObj instanceof JsonExportable ex) {
                ex.writeJson(producer);
            } else if (jObj instanceof JsonWriter jw) {
                jw.writeJson(producer);
            } else {
                throw new JsonException("cannot write " + jObj + " as json because it does not implement JsonExportable or JsonWriter");
            }
        }
        producer.endArray();
    }
}
