// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.functions;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.metadata.HasMetaData;
import com.eischet.janitor.api.metadata.MetaDataKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * Wrap this callable in a JanitorObject that carries a HELP text, e.g. for editors and the help() function.
     * @param functionName the name of the function
     * @param help the HELP text, starting with the call form, e.g. "print(values...): ..."; may be null
     * @return a JanitorObject that wraps this callable
     */
    default JanitorObject asObject(final String functionName, final @Nullable String help) {
        return new Wrapper(this, functionName, help);
    }

    /**
     * A wrapper around a JCallable that makes it a JanitorObject.
     */
    class Wrapper implements JanitorObject, JCallable, HasMetaData {
        private final JCallable callable;
        private final String name;
        private final String functionName;
        private final @Nullable String help;

        /**
         * Create a new Wrapper.
         * @param callable the callable to wrap
         * @param functionName the name of the function/method/call-it-what-you-want
         */
        public Wrapper(final @NotNull JCallable callable, final @NotNull String functionName) {
            this(callable, functionName, null);
        }

        /**
         * Create a new Wrapper with a HELP text.
         * @param callable the callable to wrap
         * @param functionName the name of the function
         * @param help the HELP text, may be null
         */
        public Wrapper(final @NotNull JCallable callable, final @NotNull String functionName, final @Nullable String help) {
            this.callable = callable;
            this.name = functionName;
            this.functionName = "[function " + functionName + "]";
            this.help = help;
        }

        @Override
        public <K> @Nullable K getMetaData(final @NotNull MetaDataKey<K> key) {
            if (key == Janitor.MetaData.NAME) {
                //noinspection unchecked
                return (K) name;
            }
            if (key == Janitor.MetaData.HELP) {
                //noinspection unchecked
                return (K) help;
            }
            return null;
        }

        @Override
        public <K> @Nullable K getMetaData(final @NotNull String attributeName, final @NotNull MetaDataKey<K> key) {
            return null;
        }

        @Override
        public @NotNull Object janitorGetHostValue() {
            return callable;
        }

        @Override
        public @NotNull String janitorToString() {
            return functionName;
        }

        /**
         * @return the name of the function
         */
        public String getFunctionName() {
            return functionName;
        }

        /**
         * @return the wrapped callable
         */
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

        @Override
        public String toString() {
            return "JCallable.Wrapper(" + functionName + ")";
        }
    }
}
