package com.eischet.janitor.compiler.ast.expression.binary;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;

/**
 * Delegate interface for binary operations.
 * Idea: They all kind of work the same way, so let's only implement what's unique for each operation.
 */
@FunctionalInterface
public interface BinaryOperationDelegate {
    /**
     * Perform the operation.
     * @param process the running script
     * @param leftValue left operand
     * @param rightValue right operand
     * @return the result
     * @throws JanitorRuntimeException on errors
     */
    JanitorObject perform(final JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException;
}
