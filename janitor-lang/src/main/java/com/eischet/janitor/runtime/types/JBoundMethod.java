package com.eischet.janitor.runtime.types;

import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.JanitorScriptProcess;
import org.jetbrains.annotations.NotNull;

import static com.eischet.janitor.tools.ObjectUtilities.simpleClassNameOf;

public class JBoundMethod<T> implements JCallable, JanitorObject {
    private final T object;
    private final JUnboundMethod<T> worker;
    private final String name;

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
    public Object janitorGetHostValue() {
        return this;
    }

    @Override
    public @NotNull String janitorClassName() {
        return "function";
    }

}
