package com.eischet.janitor.api.types;

import com.eischet.janitor.api.traits.JIterable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A set object, representing a mutable set of Janitor objects.
 * This is one of the built-in types that Janitor provides automatically.
 * There's currently no syntax to define a set directly, though.
 */
public class JSet implements JanitorObject, JIterable {

    protected final Set<JanitorObject> set = new HashSet<>();

    /**
     * Create a new JSet.
     * @param init the initial elements (copied)
     */
    public JSet(final Collection<? extends JanitorObject> init) {
        set.addAll(init);
    }

    /**
     * Create a new JSet from a stream.
     * @param init the stream
     */
    public JSet(final Stream<? extends JanitorObject> init) {
        init.forEach(set::add);
    }

    /**
     * Create a new JSet from a stream.
     * @param stream the stream
     * @return the set
     */
    public static JSet of(final Stream<? extends JanitorObject> stream) {
        return new JSet(stream);
    }

    /**
     * Create a new JSet.
     * @param valueSet the set
     * @return the set
     * @param <T> the type of the set
     */
    public static <T extends JanitorObject> JanitorObject of(final Set<T> valueSet) {
        return new JSet(valueSet);
    }

    @Override
    public Set<JanitorObject> janitorGetHostValue() {
        return set;
    }

    @Override
    public String janitorToString() {
        return set.toString();
    }

    @Override
    public boolean janitorIsTrue() {
        return !set.isEmpty();
    }

    @Override
    public Iterator<JanitorObject> getIterator() {
        return set.iterator();
    }

    /**
     * Get the size of the set.
     * @return the size
     */
    public int size() {
        return set.size();
    }

    /**
     * Add an element to the set.
     * @param value the value
     * @return true if the set did not already contain the element
     * @see HashSet#add(Object)
     */
    public boolean add(JanitorObject value) {
        return set.add(value);
    }

    /**
     * Remove an element from the set.
     * @param value the value
     * @return true if the set contained the element
     * @see HashSet#remove(Object)
     */
    public boolean remove(JanitorObject value) {
        return set.remove(value);
    }

    /**
     * Check if the set contains an element.
     * @param value the value
     * @return true if the set contains the element
     * @see HashSet#contains(Object)
     */
    public boolean contains(JanitorObject value) {
        return set.contains(value);
    }

    @Override
    public @NotNull String janitorClassName() {
        return "set";
    }

}