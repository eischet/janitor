package com.eischet.janitor.api.calls;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JCallable;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * A method bound to an object.
 * @param <T> the type of object to which this method is bound.
 */
public class JBoundMethod<T> implements JCallable, JanitorObject {
    private final T object;
    private final JUnboundMethod<T> worker;
    private final String name;

    /**
     * Create a new bound method.
     * @param name the name of the method.
     * @param object the object to which this method is bound.
     * @param worker the worker that will execute the method.
     */
    public JBoundMethod(final String name, final T object, final JUnboundMethod<T> worker) {
        this.object = object;
        this.worker = worker;
        this.name = name;
    }

    @Override
    public String toString() {
        return simpleClassNameOf(object) + "::" +  name + "()";
    }

    @Override
    public JanitorObject call(final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
        return worker.call(object, runningScript, arguments);
    }

    @Override
    public @NotNull String janitorClassName() {
        return "function";
    }

    /**
     * Create a new bound method.
     * @param name the name of the method.
     * @param object the object to which this method is bound.
     * @param worker the worker that will execute the method.
     * @return the new bound method.
     * @param <X> the type of object to which this method is bound.
     */
    public static <X> JBoundMethod<X> of(final String name, final X object, final JUnboundMethod<X> worker) {
        return new JBoundMethod<>(name, object, worker);
    }

}
