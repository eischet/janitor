package com.eischet.janitor.api.types;

import com.eischet.janitor.api.JanitorEnvironment;
import com.eischet.janitor.api.calls.TemporaryAssignable;
import com.eischet.janitor.api.traits.JIterable;
import com.eischet.janitor.toolbox.json.api.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

/**
 * A list object, representing a mutable list of Janitor objects.
 * This is one of the built-in types that Janitor provides automatically.
 */
public class JList implements JanitorObject, JIterable, Iterable<JanitorObject>, JsonExportableList {

    private final List<JanitorObject> list;

    /**
     * Create a new JList.
     */
    public JList() {
        this.list = new ArrayList<>();
    }

    /**
     * Create a new JList.
     *
     * @param initialSize the initial size
     */
    public JList(int initialSize) {
        this.list = new ArrayList<>(initialSize);
    }

    /**
     * Create a new JList.
     * The array you pass in is copied.
     *
     * @param init the initial elements
     */
    public JList(final Collection<? extends JanitorObject> init) {
        this.list = new ArrayList<>(init);
    }

    /**
     * Create a new JList from a stream.
     *
     * @param init the stream
     */
    public JList(final Stream<? extends JanitorObject> init) {
        this();
        init.forEach(list::add);
    }

    /**
     * Create a new JList from a Java list.
     *
     * @param elements the list
     */
    public JList(final List<JanitorObject> elements) {
        this();
        list.addAll(elements);
    }

    /**
     * Create a new JList from another JList.
     * The original list is copied.
     *
     * @param source the source list
     */
    public JList(final JList source) {
        list = new ArrayList<>(source.list);
    }

    /**
     * Convert a Python-like index into an actual list index.
     * E.g. -1 points to the last element.
     *
     * @param index the index
     * @param len   the length of the list
     * @return the actual "physical" index
     */
    public static int toIndex(final int index, final int len) {
        if (index >= 0) {
            return index;
        } else {
            return len + index; // Pythonic: -1 is the last element, etc.; +i because i is negative :o)
        }
    }

    /**
     * Create a new JList.
     *
     * @param valueList the list
     * @return the list
     */
    public static JList of(final List<? extends JanitorObject> valueList) {
        return new JList(valueList);
    }

    /**
     * Create a new JList.
     *
     * @param valueStream the stream
     * @return the list
     */
    public static JList of(final Stream<? extends JanitorObject> valueStream) {
        return new JList(valueStream);
    }

    /**
     * Read a JSON string, representing a list, into this list.
     *
     * @param json the JSON string
     * @param env  the environment
     * @return this list
     * @throws JsonException if the JSON is invalid, e.g. it's not really a list
     */
    public JList parseJson(final String json, final JanitorEnvironment env) throws JsonException {
        if (json == null || json.isBlank()) {
            return this;
        }
        final JsonInputStream reader = env.getLenientJsonConsumer(json);
        return parseJson(reader, env);
    }

    /**
     * Read a JSON string, representing a list, into this list.
     *
     * @param reader the JSON reader
     * @return this list
     * @throws JsonException if the JSON is invalid, e.g. it's not really a list
     */
    public JList parseJson(final JsonInputStream reader, final JanitorEnvironment env) throws JsonException {
        reader.beginArray();
        while (reader.hasNext()) {
            add(JCollection.parseJsonValue(reader, env));
        }
        reader.endArray();
        return this;
    }

    /**
     * Get the size of the list.
     *
     * @return the size
     */
    public int size() {
        return list.size();
    }

    /**
     * Get the element at the given (physical) index.
     *
     * @param index the index
     * @return the element
     */
    public JanitorObject get(int index) {
        return list.get(index);
    }

    /**
     * Get the element at the given (physical or "pythonical") index.
     *
     * @param index the index
     * @return the element
     */
    public JanitorObject get(JInt index) {
        return list.get(toIndex(index.getAsInt(), list.size()));
    }

    /**
     * Get the element at the given (physical or "pythonical") index.
     * This is not meant for client code, but for internal use in the interpreter.
     * Useful Quirk: this index can be assigned to, e.g. "foo[3] = 'bar'".
     *
     * @param index the index
     * @return the element
     */
    public JanitorObject getIndexed(JInt index) {
        return new TemporaryAssignable(get(index), value -> list.set(toIndex(index.getAsInt(), list.size()), value));
    }

    /**
     * Get a range of elements.
     *
     * @param start the start index
     * @param end   the end index
     * @return the range
     */
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

    /**
     * Add an element to the list.
     *
     * @param i     the index
     * @param value the value
     */
    public void add(JInt i, JanitorObject value) {
        list.add(i.janitorGetHostValue().intValue(), value);
    }

    /**
     * Add an element to the list.
     *
     * @param value the value
     */
    public void add(JanitorObject value) {
        list.add(value.janitorUnpack());
    }

    /**
     * Remove an element from the list.
     *
     * @param value the value
     */
    public void remove(JanitorObject value) {
        list.remove(value.janitorUnpack());
    }

    /**
     * Replace an element in the list.
     *
     * @param index the index
     * @param value the value
     */
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

    /**
     * Define truthiness: list is not empty.
     *
     * @return true if the list is not empty
     */
    @Override
    public boolean janitorIsTrue() {
        return !list.isEmpty();
    }

    @Override
    public Iterator<JanitorObject> getIterator() {
        return list.iterator();
    }

    /**
     * Get a stream of the elements.
     *
     * @return the stream
     */
    public Stream<JanitorObject> stream() {
        return list.stream();
    }

    /**
     * Remote and return the first element of the list, like a stack where #0 is at the top, or like a queue.
     *
     * @return the first element, or NULL if the list was empty
     */
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

    /**
     * Check if the list is empty.
     *
     * @return true if the list is empty
     */
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
