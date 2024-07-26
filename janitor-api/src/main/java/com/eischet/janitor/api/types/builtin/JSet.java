package com.eischet.janitor.api.types.builtin;

import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.wrapper.JanitorWrapper;
import com.eischet.janitor.api.types.JIterable;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A set object, representing a mutable set of Janitor objects.
 * This is one of the built-in types that Janitor provides automatically.
 * There's currently no syntax to define a set directly, though.
 */
public class JSet extends JanitorWrapper<Set<JanitorObject>> implements JanitorObject, JIterable {

    private JSet(final Dispatcher<JanitorWrapper<Set<JanitorObject>>> dispatcher, final Set<JanitorObject> set) {
        super(dispatcher, set);
    }


    @Override
    public Set<JanitorObject> janitorGetHostValue() {
        return wrapped;
    }

    @Override
    public String janitorToString() {
        return wrapped.toString();
    }

    @Override
    public boolean janitorIsTrue() {
        return !wrapped.isEmpty();
    }

    @Override
    public Iterator<JanitorObject> getIterator() {
        return wrapped.iterator();
    }

    /**
     * Get the size of the set.
     * @return the size
     */
    public int size() {
        return wrapped.size();
    }

    /**
     * Add an element to the set.
     * @param value the value
     * @return true if the set did not already contain the element
     * @see HashSet#add(Object)
     */
    public boolean add(JanitorObject value) {
        return wrapped.add(value);
    }

    /**
     * Remove an element from the set.
     * @param value the value
     * @return true if the set contained the element
     * @see HashSet#remove(Object)
     */
    public boolean remove(JanitorObject value) {
        return wrapped.remove(value);
    }

    /**
     * Check if the set contains an element.
     * @param value the value
     * @return true if the set contains the element
     * @see HashSet#contains(Object)
     */
    public boolean contains(JanitorObject value) {
        return wrapped.contains(value);
    }

    @Override
    public @NotNull String janitorClassName() {
        return "set";
    }


    public static JSet newInstance(final Dispatcher<JanitorWrapper<Set<JanitorObject>>> dispatcher, final Set<JanitorObject> set) {
        return new JSet(dispatcher, set);
    }

}