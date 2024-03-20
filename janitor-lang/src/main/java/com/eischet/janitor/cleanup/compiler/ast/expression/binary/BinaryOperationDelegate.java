package com.eischet.janitor.cleanup.compiler.ast.expression.binary;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;

@FunctionalInterface
public interface BinaryOperationDelegate {
    JanitorObject perform(final JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException;
}
