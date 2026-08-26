package com.eischet.janitor.api.types.builtin;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.TemporaryAssignable;
import com.eischet.janitor.api.types.composed.JanitorComposed;
import com.eischet.janitor.api.types.dispatch.DispatchTable;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.JIterable;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.toolbox.json.api.*;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
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
        final int len = list.size();
        final int resolved = toIndex(index.getAsInt(), len);
        if (resolved < 0 || resolved >= len) {
            throw new IndexOutOfBoundsException("list index " + index.getAsInt() + " out of range for list of size " + len);
        }
        return list.get(resolved);
    }

    /**
     * Get the element at the given (physical or "pythonical") index.
     * This is not meant for client code, but for internal use in the interpreter.
     * Useful Quirk: this index can be assigned to, e.g. "foo[3] = 'bar'".
     * <p>
     * Unlike range/slice assignment (see {@link #getAssignableRange}), single-index assignment never
     * grows the list, exactly like real Python: the index must already exist, or this throws.
     *
     * @param index the index
     * @return the element
     */
    public JanitorObject getIndexed(JInt index) {
        return TemporaryAssignable.of(
                "[" + index.janitorToString() + "]",
                get(index),
                value -> {
                    final int len = list.size();
                    final int resolved = toIndex(index.getAsInt(), len);
                    if (resolved < 0 || resolved >= len) {
                        throw new IndexOutOfBoundsException("list assignment index " + index.getAsInt() + " out of range for list of size " + len);
                    }
                    list.set(resolved, value);
                    notifyUpdateReceivers();
                }
                );
    }

    /**
     * Get a range of elements, Python-style: out-of-range bounds are silently clamped into
     * {@code [0, size()]}, and a descending range (the resolved end at or before the resolved
     * start) reads as empty -- e.g. {@code [1,2,3][1:100] == [1,2,3][1:]} and {@code [1,2,3][2:1] == []}.
     * This is deliberately forgiving, unlike {@link #setRange} -- see JListPythonIndexingTestCase/
     * JListTestCase for the rationale behind why slice *assignment* is stricter than reading.
     *
     * @param start the start index
     * @param end   the end index
     * @return the range, as a fresh, independent list (never a view of this list)
     */
    public JanitorObject getRange(JInt start, JInt end) {
        final int len = list.size();
        final int startIndex = clamp(toIndex(start.getAsInt(), len), 0, len);
        final int endIndex = clamp(toIndex(end.getAsInt(), len), 0, len);
        if (endIndex <= startIndex) {
            return new JList(dispatcher, new ArrayList<>());
        }
        // Must copy here: List.subList() returns a live view backed by the source list, not a
        // snapshot. Without the copy, mutating the returned "slice" (add/remove/set) would mutate
        // the source list too.
        return new JList(dispatcher, new ArrayList<>(list.subList(startIndex, endIndex)));
    }

    /**
     * Get an assignable range of elements: reading it behaves exactly like {@link #getRange}, but it
     * can also be assigned to (e.g. {@code list[1:3] = [10, 20, 30]}), which replaces the selected
     * elements with the assigned collection, growing or shrinking this list as needed -- see
     * {@link #setRange} for the exact (stricter than read) rules for the bounds.
     *
     * @param start the start index
     * @param end   the end index
     * @return an assignable view of the range
     */
    public JanitorObject getAssignableRange(final JInt start, final JInt end) {
        return TemporaryAssignable.of(
                "[" + start.janitorToString() + ":" + end.janitorToString() + "]",
                getRange(start, end),
                replacement -> setRange(start, end, replacement)
        );
    }

    /**
     * Get a range of elements with a step, Python-style (e.g. {@code list[::-1]} for a reversed
     * copy, or {@code list[1:8:2]}). Bounds are clamped exactly like {@link #getRange}; a positive
     * step iterates forward defaulting to the whole list ({@code 0} to {@code size()}) when a bound
     * is omitted, a negative step iterates backward defaulting to the whole list in reverse
     * ({@code size()-1} down to, but not including, {@code -1}) when a bound is omitted -- this is
     * exactly what makes {@code list[::-1]} mean "the whole list, reversed".
     *
     * @param start the start index, or null if omitted ("list[:...]")
     * @param end   the end index, or null if omitted ("list[...:]")
     * @param step  the step, must not be 0
     * @return the resulting range, as a fresh, independent list
     */
    public JanitorObject getSteppedRange(final Integer start, final Integer end, final int step) {
        if (step == 0) {
            throw new IllegalArgumentException("slice step cannot be zero");
        }
        final int len = list.size();
        final int startIndex;
        final int endIndex;
        if (step > 0) {
            startIndex = start == null ? 0 : clamp(toIndex(start, len), 0, len);
            endIndex = end == null ? len : clamp(toIndex(end, len), 0, len);
        } else {
            startIndex = start == null ? len - 1 : clamp(toIndex(start, len), -1, len - 1);
            endIndex = end == null ? -1 : clamp(toIndex(end, len), -1, len - 1);
        }
        final List<JanitorObject> result = new ArrayList<>();
        if (step > 0) {
            for (int i = startIndex; i < endIndex; i += step) {
                result.add(list.get(i));
            }
        } else {
            for (int i = startIndex; i > endIndex; i += step) {
                result.add(list.get(i));
            }
        }
        return new JList(dispatcher, result);
    }

    private static int clamp(final int value, final int min, final int max) {
        return Math.max(min, Math.min(value, max));
    }

    /**
     * Replace the elements in the range {@code [start, end)} with the elements of
     * {@code replacementValue} (any iterable, typically a list), growing or shrinking this list as
     * needed to accommodate a replacement of a different size than the selected range -- e.g.
     * {@code list[1:3] = [10, 20, 30, 40]} replaces 2 elements with 4, growing the list by 2.
     * <p>
     * Unlike {@link #getRange}, the bounds here are validated strictly, not clamped: after resolving
     * negative indices, both {@code start} and {@code end} must fall into {@code [0, size()]} (that
     * upper bound is a valid, meaningful position: right after the last element, i.e. a pure
     * append), or this throws -- deliberately deviating from real Python (which clamps here too),
     * so that a typo like {@code list[1:1000] = [...]} fails loudly instead of silently succeeding
     * in a probably-unintended way.
     * <p>
     * If the resolved start is greater than the resolved end (e.g. {@code list[3:1] = [...]}), this
     * follows real Python: the selection is empty, and the replacement is inserted at {@code start}
     * without removing anything.
     *
     * @param start           the start index
     * @param end             the end index
     * @param replacementValue the replacement elements (must be iterable)
     */
    public void setRange(final JInt start, final JInt end, final JanitorObject replacementValue) {
        final int len = list.size();
        final int startIndex = toIndex(start.getAsInt(), len);
        final int endIndex = toIndex(end.getAsInt(), len);
        if (startIndex < 0 || startIndex > len) {
            throw new IndexOutOfBoundsException("slice assignment start index " + start.getAsInt() + " out of range for list of size " + len);
        }
        if (endIndex < 0 || endIndex > len) {
            throw new IndexOutOfBoundsException("slice assignment end index " + end.getAsInt() + " out of range for list of size " + len);
        }
        final List<JanitorObject> replacement = toReplacementList(replacementValue);
        final int removeEnd = Math.max(startIndex, endIndex);
        list.subList(startIndex, removeEnd).clear();
        list.addAll(startIndex, replacement);
        notifyUpdateReceivers();
    }

    private static List<JanitorObject> toReplacementList(final JanitorObject replacementValue) {
        final JanitorObject unpacked = replacementValue.janitorUnpack();
        if (unpacked instanceof Iterable<?> iterable) {
            final List<JanitorObject> result = new ArrayList<>();
            for (final Object element : iterable) {
                result.add((JanitorObject) element);
            }
            return result;
        }
        throw new IllegalArgumentException("cannot assign " + replacementValue + " to a list slice: not iterable");
    }

    /**
     * Add an element to the list.
     *
     * @param index the index
     * @param value the value
     */
    public void add(JInt index, JanitorObject value) {
        list.add(index.janitorGetHostValue().intValue(), value);
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

    /**
     * Remove all elements from the list.
     */
    @Contract(mutates = "this")
    public void clear() {
        list.clear();
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

    public @Language("JSON") String exportToJson() throws JsonException {
        return exportToJson(Janitor.current());
    }

}
