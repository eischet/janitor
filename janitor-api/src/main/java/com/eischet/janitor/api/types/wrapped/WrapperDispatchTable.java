package com.eischet.janitor.api.types.wrapped;

import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.dispatch.Dispatcher;
import com.eischet.janitor.api.types.dispatch.GenericDispatchTable;

import java.lang.reflect.Constructor;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Dispatch Table Variant for JanitorWrapper descendants.
 * @param <T> the wrapped type
 */
public class WrapperDispatchTable<T> extends GenericDispatchTable<JanitorWrapper<T>> {

    /**
     * Create a new dispatch table.
     * This is used for "root" objects or for objects where it is not necessary
     * to simulate any form of inheritance.
     */
    public WrapperDispatchTable(final Supplier<JanitorWrapper<T>> javaDefaultConstructor) {
        super(javaDefaultConstructor);
    }

    public WrapperDispatchTable() {
        super(null);
    }

    /**
     * Create a new dispatch table that uses a parent or "super" table to look um super methods.
     * @param parent the parent dispatch table
     * @param caster a function that casts up to the parent type, because Java cannot provide that automatically
     * @param <P> the type of the parent dispatch table
     */
    public <P extends JanitorObject> WrapperDispatchTable(final Dispatcher<P> parent,
                                                          final Function<JanitorWrapper<T>, P> caster) {
        super(parent, caster);
    }
}
