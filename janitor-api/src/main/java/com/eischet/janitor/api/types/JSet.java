package com.eischet.janitor.api.types;

import com.eischet.janitor.api.traits.JIterable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

public class JSet implements JanitorObject, JIterable {

    protected final Set<JanitorObject> set = new HashSet<>();

    public JSet(final Collection<? extends JanitorObject> init) {
        set.addAll(init);
    }

    public JSet(final Stream<? extends JanitorObject> init) {
        init.forEach(set::add);
    }

    public static JanitorObject of(final Stream<? extends JanitorObject> stream) {
        return new JSet(stream);
    }

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

    public int size() {
        return set.size();
    }

    public boolean add(JanitorObject value) {
        return set.add(value);
    }

    public boolean remove(JanitorObject value) {
        return set.remove(value);
    }

    public boolean contains(JanitorObject value) {
        return set.contains(value);
    }

    @Override
    public @NotNull String janitorClassName() {
        return "set";
    }

}