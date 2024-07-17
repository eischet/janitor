package com.eischet.janitor.compiler.ast.expression.unary;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;

/**
 * Delegate interface for implementing unary operations.
 */
@FunctionalInterface
public interface UnaryOperationDelegate {
    JanitorObject perform(final JanitorScriptProcess process, final JanitorObject parameter) throws JanitorRuntimeException;
}
