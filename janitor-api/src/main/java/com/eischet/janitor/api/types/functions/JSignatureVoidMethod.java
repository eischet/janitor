// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.api.types.functions;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorError;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;

/**
 * A method without a result whose arguments a {@link Signature} has already bound; scripts get null.
 * @param <T> the type of object that the method is called on
 */
@FunctionalInterface
public interface JSignatureVoidMethod<T> {
    /**
     * Calls the method.
     * @param self the object that the method is called on
     * @param process the running script process
     * @param arguments the arguments, by parameter name
     * @throws JanitorRuntimeException if the call fails
     * @throws JanitorError on internal errors
     */
    void call(T self, JanitorScriptProcess process, SignatureArguments arguments) throws JanitorRuntimeException, JanitorError;
}
