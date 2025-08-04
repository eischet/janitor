package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Dispatch Table variant for most object.
 * @see com.eischet.janitor.api.types.composed.JanitorComposed for the main user of this class
 * @see com.eischet.janitor.api.types.wrapped.WrapperDispatchTable for a variant specialized on wrappers
 * @param <T> any type of JanitorObject
 */
public class DispatchTable<T extends JanitorObject> extends GenericDispatchTable<T> {

    /**
     * Create a "root" dispatch table.
     */
    public DispatchTable(final @Nullable Supplier<T> javaDefaultConstructor) {
        super(javaDefaultConstructor);
    }

    public DispatchTable() {
        super(null);
    }

    /**
     * Create a "child" or "subclass" dispatch table.
     * @param parent the parent or super "class" dispatch table
     * @param caster a function to case from our T type to the parent P type, because Java cannot provide this automatically
     * @param <P> the type of the parent dispatch table
     */
    public <P extends JanitorObject> DispatchTable(final Dispatcher<P> parent, final Function<T, P> caster) {
        super(parent, caster);
    }

}
