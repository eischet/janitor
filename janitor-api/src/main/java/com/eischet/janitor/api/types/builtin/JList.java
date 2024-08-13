package com.eischet.janitor.api.types.builtin;

import com.eischet.janitor.api.types.TemporaryAssignable;
import com.eischet.janitor.api.types.wrapped.JanitorWrapperDispatchTable;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapped.JanitorWrapper;
import com.eischet.janitor.api.types.JIterable;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.toolbox.json.api.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.stream.Stream;

/**
 * A list object, representing a mutable list of Janitor objects.
 * This is one of the built-in types that Janitor provides automatically.
 */
public class JList extends JanitorWrapper<List<JanitorObject>> implements JIterable, Iterable<JanitorObject>, JsonExportableList {

    private JList(final Dispatcher<JanitorWrapper<List<JanitorObject>>> dispatcher, final List<JanitorObject> list) {
        super(dispatcher, list);
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
     * Create a new instance and <b>take ownership of the list passed to us.</b>
     * @param listDispatcher method dispatcher
     * @param objects initial list
     * @return this
     */
    public static JList newInstance(final JanitorWrapperDispatchTable<List<JanitorObject>> listDispatcher, final List<JanitorObject> objects) {
        return new JList(listDispatcher, objects);
    }



    /**
     * Get the size of the list.
     *
     * @return the size
     */
    public int size() {
        return wrapped.size();
    }

    /**
     * Get the element at the given (physical) index.
     *
     * @param index the index
     * @return the element
     */
    public JanitorObject get(int index) {
        return wrapped.get(index);
    }

    /**
     * Get the element at the given (physical or "pythonical") index.
     *
     * @param index the index
     * @return the element
     */
    public JanitorObject get(JInt index) {
        return wrapped.get(toIndex(index.getAsInt(), wrapped.size()));
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
        return new TemporaryAssignable(get(index), value -> wrapped.set(toIndex(index.getAsInt(), wrapped.size()), value));
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
        final int startIndex = toIndex(start.getAsInt(), wrapped.size());
        final int endIndex = toIndex(end.getAsInt(), wrapped.size());
        final List<JanitorObject> subList = wrapped.subList(Math.min(startIndex, endIndex), Math.max(startIndex, endIndex));
        if (endIndex < startIndex) {
            Collections.reverse(subList);
        }
        return new JList(dispatcher, subList);
    }

    /**
     * Add an element to the list.
     *
     * @param i     the index
     * @param value the value
     */
    public void add(JInt i, JanitorObject value) {
        wrapped.add(i.janitorGetHostValue().intValue(), value);
    }

    /**
     * Add an element to the list.
     *
     * @param value the value
     */
    public void add(JanitorObject value) {
        wrapped.add(value.janitorUnpack());
    }

    /**
     * Remove an element from the list.
     *
     * @param value the value
     */
    public void remove(JanitorObject value) {
        wrapped.remove(value.janitorUnpack());
    }

    /**
     * Replace an element in the list.
     *
     * @param index the index
     * @param value the value
     */
    public void put(JInt index, JanitorObject value) {
        wrapped.set(index.janitorGetHostValue().intValue(), value);
    }

    @Override
    public @Unmodifiable List<JanitorObject> janitorGetHostValue() {
        return List.copyOf(wrapped);
    }

    /**
     * Define truthiness: list is not empty.
     *
     * @return true if the list is not empty
     */
    @Override
    public boolean janitorIsTrue() {
        return !wrapped.isEmpty();
    }

    @Override
    public Iterator<JanitorObject> getIterator() {
        return wrapped.iterator();
    }

    /**
     * Get a stream of the elements.
     *
     * @return the stream
     */
    public Stream<JanitorObject> stream() {
        return wrapped.stream();
    }

    /**
     * Remote and return the first element of the list, like a stack where #0 is at the top, or like a queue.
     *
     * @return the first element, or NULL if the list was empty
     */
    public JanitorObject popFirst() {
        if (wrapped.isEmpty()) {
            return JNull.NULL;
        } else {
            return wrapped.remove(0);
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
        return wrapped.isEmpty();
    }

    @Override
    public boolean isDefaultOrEmpty() {
        return isEmpty();
    }

    @Override
    public void writeJson(final JsonOutputStream producer) throws JsonException {
        producer.beginArray();
        for (final JanitorObject jObj : wrapped) {
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
