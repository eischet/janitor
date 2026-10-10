// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.compiler.ast.expression.unary;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;

/**
 * Delegate interface for implementing unary operations.
 */
@FunctionalInterface
public interface UnaryOperationDelegate {
    /**
     * Performs the operation.
     * @param process the running script process
     * @param parameter the operand
     * @return the result
     * @throws JanitorRuntimeException if the operation fails
     */
    JanitorObject perform(final JanitorScriptProcess process, final JanitorObject parameter) throws JanitorRuntimeException;
}
