package com.eischet.janitor.api.traits;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.calls.JCallArgs;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface JCallable {
    JanitorObject call(final JanitorScriptProcess runningScript, JCallArgs arguments) throws JanitorRuntimeException;

    default JanitorObject asObject(final String functionName) {
        return new Wrapper(this, functionName);
    }

    class Wrapper implements JanitorObject, JCallable {
        private final JCallable callable;
        private final String functionName;

        public Wrapper(final JCallable callable, final String functionName) {
            this.callable = callable;
            this.functionName = "[function " + functionName + "]";
        }

        @Override
        public Object janitorGetHostValue() {
            return callable;
        }

        @Override
        public String janitorToString() {
            return functionName;
        }

        @Override
        public JanitorObject call(final JanitorScriptProcess runningScript, final JCallArgs arguments) throws JanitorRuntimeException {
            return callable.call(runningScript, arguments);
        }

        @Override
        public @NotNull String janitorClassName() {
            return "function";
        }

    }
}
