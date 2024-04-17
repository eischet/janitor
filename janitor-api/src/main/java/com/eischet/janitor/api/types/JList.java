package com.eischet.janitor.api.types;

import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.calls.TemporaryAssignable;
import com.eischet.janitor.api.json.api.*;
import com.eischet.janitor.api.traits.JIterable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public class JList implements JanitorObject, JIterable, Iterable<JanitorObject>, JsonExportableList {

    private final List<JanitorObject> list;

    public JList() {
        this.list = new ArrayList<>();
    }

    public JList(int initialSize) {
        this.list = new ArrayList<>(initialSize);
    }

    public JList(final Collection<? extends JanitorObject> init) {
        this.list = new ArrayList<>(init);
    }

    public JList(final Stream<? extends JanitorObject> init) {
        this();
        init.forEach(list::add);
    }

    public JList(final List<JanitorObject> elements) {
        this();
        list.addAll(elements);
    }

    public JList(final JList source) {
        list = new ArrayList<>(source.list);
    }

    public static int toIndex(final int index, final int len) {
        if (index >= 0) {
            return index;
        } else {
            return len + index; // Pythonic: -1 is the last element, etc.; +i because i is negative :o)
        }
    }

    public static JList of(final List<? extends JanitorObject> valueList) {
        return new JList(valueList);
    }

    public static JList of(final Stream<? extends JanitorObject> valueStream) {
        return new JList(valueStream);
    }

    public JList parseJson(final String json, final JanitorEnvironment env) throws JsonException {
        if (json == null || json.isBlank()) {
            return this;
        }
        final JsonInputStream reader = env.getLenientJsonConsumer(json);
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

    public int size() {
        return list.size();
    }

    public JanitorObject get(int index) {
        return list.get(index);
    }

    public JanitorObject get(JInt index) {
        return list.get(toIndex(index.getAsInt(), list.size()));
    }

    public JanitorObject getIndexed(JInt index) {
        return new TemporaryAssignable(get(index), value -> list.set(toIndex(index.getAsInt(), list.size()), value));
    }

    public JanitorObject getRange(JInt start, JInt end) {
        // LATER: stepping
        // LATER: wrap in TemporaryAssignable for things like list[10:] = ["rest", "of", "list"];
        final int startIndex = toIndex(start.getAsInt(), list.size());
        final int endIndex = toIndex(end.getAsInt(), list.size());
        final List<JanitorObject> subList = list.subList(Math.min(startIndex, endIndex), Math.max(startIndex, endIndex));
        if (endIndex < startIndex) {
            Collections.reverse(subList);
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

    @Override
    public List<JanitorObject> janitorGetHostValue() {
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
