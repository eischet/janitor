package com.eischet.janitor.api.types.dispatch;

import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Dispatch Table variant for most object.
 *
 * @param <T> any type of JanitorObject
 * @see com.eischet.janitor.api.types.composed.JanitorComposed for the main user of this class
 * @see com.eischet.janitor.api.types.wrapped.WrapperDispatchTable for a variant specialized on wrappers
 */
public class DispatchTable<T extends JanitorObject> extends GenericDispatchTable<T> {

    /**
     * Create a "root" dispatch table.
     *
     * @param javaDefaultConstructor a default constructor for the type
     * @param includeApplyMethod     whether to include the apply method
     */
    public DispatchTable(final @Nullable Supplier<T> javaDefaultConstructor, final boolean includeApplyMethod) {
        super(javaDefaultConstructor, includeApplyMethod);
    }

    /**
     * Create a "root" dispatch table.
     *
     * @param javaDefaultConstructor a default constructor for the type
     */
    public DispatchTable(final @Nullable Supplier<T> javaDefaultConstructor) {
        super(javaDefaultConstructor, true);
    }

    /**
     * Create a "root" dispatch table.
     *
     * @param includeApplyMethod whether to include the apply method
     */
    public DispatchTable(final boolean includeApplyMethod) {
        super(null, includeApplyMethod);
    }

    /**
     * Create a "root" dispatch table.
     */
    public DispatchTable() {
        super(null, true);
    }

    /**
     * Create a "child" or "subclass" dispatch table.
     *
     * @param parent             the parent or super "class" dispatch table
     * @param caster             a function to case from our T type to the parent P type, because Java cannot provide this automatically
     * @param includeApplyMethod whether to include the apply method
     * @param <P>                the type of the parent dispatch table
     */
    public <P extends JanitorObject> DispatchTable(final Dispatcher<P> parent, final Function<T, P> caster, final boolean includeApplyMethod) {
        super(parent, caster, includeApplyMethod);
    }

    /**
     * Convenience method to create a new dispatch table that extends this one.
     * <p>
     * Use this in a JanitorComposed constructor when you need to have instance-specific properties, for example.
     * You can then have a static "DISPATCH" and an instance-specific "dispatch" which extends the static one.
     * </p>
     *
     * @param includeApplyMethod whether to include the apply method
     * @return a new dispatch table that extends this one
     */
    public DispatchTable<T> extend(final boolean includeApplyMethod) {
        return new DispatchTable<>(this, it -> it, true);
    }

    public DispatchTable<T> extend() {
        return extend(true);
    }

}
