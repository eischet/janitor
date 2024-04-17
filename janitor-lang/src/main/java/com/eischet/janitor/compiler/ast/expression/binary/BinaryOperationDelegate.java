package com.eischet.janitor.compiler.ast.expression.binary;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;

@FunctionalInterface
public interface BinaryOperationDelegate {
    JanitorObject perform(final JanitorScriptProcess process, final JanitorObject leftValue, final JanitorObject rightValue) throws JanitorRuntimeException;
}
