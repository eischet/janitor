// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.functions;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;

/**
 * A method that does not return a value.
 * @param <T> the type of object that this method calls "this".
 */
@FunctionalInterface
public interface JVoidMethod<T> {
    /**
     * Calls the method.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the call arguments
     * @throws JanitorRuntimeException if the call fails
     */
    void call(final T self, final JanitorScriptProcess process, final JCallArgs arguments) throws JanitorRuntimeException;
}
