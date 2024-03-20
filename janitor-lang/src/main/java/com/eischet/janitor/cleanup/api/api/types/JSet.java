package com.eischet.janitor.cleanup.api.api.types;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorNameException;
import com.eischet.janitor.cleanup.runtime.types.JIterable;
import org.eclipse.collections.api.collection.ImmutableCollection;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.stream.Stream;

public class JSet implements JanitorObject, JIterable {

    private static final JSetClass myClass = new JSetClass();

    protected final MutableSet<JanitorObject> set = Sets.mutable.empty();

    public JSet() {
    }

    public JSet(final ImmutableCollection<? extends JanitorObject> init) {
        set.addAllIterable(init);
    }

    public JSet(final Stream<? extends JanitorObject> init) {
        init.forEach(set::add);
    }

    @Override
    public @Nullable JanitorObject janitorGetAttribute(final JanitorScriptProcess runningScript, final String name, final boolean required) throws JanitorNameException {
        final JanitorObject boundMethod = myClass.getBoundMethod(name, this);
        if (boundMethod != null) {
            return boundMethod;
        }
        return JanitorObject.super.janitorGetAttribute(runningScript, name, required);
    }

    public static JanitorObject of(final Stream<? extends JanitorObject> csObjStream) {
        return new JSet(csObjStream);
    }

    public static <T extends JanitorObject> JanitorObject of(final ImmutableSet<T> valueSet) {
        return new JSet(valueSet);
    }

    @Override
    public MutableSet<JanitorObject> janitorGetHostValue() {
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