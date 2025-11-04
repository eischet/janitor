package com.eischet.janitor.api.types.functions;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import org.jetbrains.annotations.NotNull;

/**
 * An object that can be called, like a function or method.
 * Janitor uses the famous "thing()" syntax for this.
 * Various implementations help writing callable code.
 */
@FunctionalInterface
public interface JCallable {

    /**
     * Call this callable. The interpreter will invoke this, not you.
     *
     * @param process the running script
     * @param arguments the arguments to pass to the callable
     * @return the result of the call
     * @throws JanitorRuntimeException if the call failed spectacularly
     */
    JanitorObject call(final JanitorScriptProcess process, JCallArgs arguments) throws JanitorRuntimeException;

    /**
     * Wrap this callable in a JanitorObject.
     * @param functionName the name of the function
     * @return a JanitorObject that wraps this callable
     */
    default JanitorObject asObject(final String functionName) {
        return new Wrapper(this, functionName);
    }

    /**
     * A wrapper around a JCallable that makes it a JanitorObject.
     */
    class Wrapper implements JanitorObject, JCallable {
        private final JCallable callable;
        private final String functionName;

        /**
         * Create a new Wrapper.
         * @param callable the callable to wrap
         * @param functionName the name of the function/method/call-it-what-you-want
         */
        public Wrapper(final @NotNull JCallable callable, final @NotNull String functionName) {
            this.callable = callable;
            this.functionName = "[function " + functionName + "]";
        }

        @Override
        public @NotNull Object janitorGetHostValue() {
            return callable;
        }

        @Override
        public @NotNull String janitorToString() {
            return functionName;
        }

        public String getFunctionName() {
            return functionName;
        }

        public JCallable getCallable() {
            return callable;
        }

        @Override
        public JanitorObject call(final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException {
            return callable.call(process, arguments);
        }

        @Override
        public @NotNull String janitorClassName() {
            return "Function";
        }

    }
}
