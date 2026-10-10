// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.functions;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorError;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;

/**
 * A method that is not bound to an object instance, but needs an instance of the object to be called.
 * @param <T> the type of object that this method calls "this".
 */
@FunctionalInterface
public interface JUnboundMethod<T> {
    /**
     * Calls the method.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @return the result of the call
     * @throws JanitorRuntimeException if the call fails
     * @throws JanitorError on internal errors
     */
    JanitorObject call(final T self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException, JanitorError;
}
