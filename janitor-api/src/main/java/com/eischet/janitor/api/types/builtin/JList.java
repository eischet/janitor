package com.eischet.janitor.api.types.builtin;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.TemporaryAssignable;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.JIterable;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.toolbox.json.api.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A list object, representing a mutable list of Janitor objects.
 * This is one of the built-in types that Janitor provides automatically.
 */
public class JList extends JanitorComposed<JList> implements JIterable, Iterable<JanitorObject>, JsonExportableList {

    private final List<JanitorObject> list;
    private List<Consumer<JList>> updateReceivers;
    private DispatchTable<?> elementDispatchTable;

    private JList(final Dispatcher<JList> dispatcher, final List<JanitorObject> list) {
        super(dispatcher);
        this.list = list;
    }

    public JList withElementDispatchTable(final DispatchTable<?> elementDispatchTable) {
        setElementDispatchTable(elementDispatchTable);
        return this;
    }

    public void setElementDispatchTable(final DispatchTable<?> elementDispatchTable) {
        this.elementDispatchTable = elementDispatchTable;
    }

    public DispatchTable<?> getElementDispatchTable() {
        return elementDispatchTable;
    }




    public @NotNull JList onUpdate(final @NotNull Consumer<JList> onUpdate) {
        if (updateReceivers == null) {
            updateReceivers = new LinkedList<>();
        }
        updateReceivers.add(onUpdate);
        return this;
    }

    public int countOnUpdateReceivers() {
        return updateReceivers == null ? 0 : updateReceivers.size();
    }

    private void notifyUpdateReceivers() {
        if (updateReceivers != null && !updateReceivers.isEmpty()) {
            for (final Consumer<JList> updateReceiver : updateReceivers) {
                updateReceiver.accept(this);
            }
        }
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
    public static JList newInstance(final DispatchTable<JList> listDispatcher, final List<JanitorObject> objects) {
        return new JList(listDispatcher, objects);
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
        return TemporaryAssignable.of(
                "[" + index.janitorToString() + "]",
                get(index),
                value -> {
                    list.set(toIndex(index.getAsInt(), list.size()), value);
                    notifyUpdateReceivers();
                }
                );
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
        return new JList(dispatcher, subList);
    }

    /**
     * Add an element to the list.
     *
     * @param i     the index
     * @param value the value
     */
    public void add(JInt i, JanitorObject value) {
        list.add(i.janitorGetHostValue().intValue(), value);
        notifyUpdateReceivers();
    }

    /**
     * Add an element to the list.
     *
     * @param value the value
     */
    public void add(JanitorObject value) {
        list.add(value.janitorUnpack());
        notifyUpdateReceivers();
    }

    /**
     * Remove an element from the list.
     *
     * @param value the value
     */
    public void remove(JanitorObject value) {
        final JanitorObject removing = value.janitorUnpack();
        replaceAllElements(stream().filter(e -> !Janitor.Semantics.areEquals(e, removing).janitorIsTrue()).toList());
    }

    /**
     * Replace an element in the list.
     *
     * @param index the index
     * @param value the value
     */
    public void put(JInt index, JanitorObject value) {
        list.set(index.janitorGetHostValue().intValue(), value);
        notifyUpdateReceivers();
    }

    public void replaceAllElements(final List<JanitorObject> withTheseElements) {
        list.clear();
        list.addAll(withTheseElements);
        notifyUpdateReceivers();
    }


    @Override
    public @Unmodifiable @NotNull List<JanitorObject> janitorGetHostValue() {
        return List.copyOf(list);
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
            final JanitorObject removed = list.remove(0);
            notifyUpdateReceivers();
            return removed;
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

    @Override
    public String toString() {
        return list.toString();
    }


}
