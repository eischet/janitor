package com.eischet.janitor.cleanup.compiler.ast.expression.unary;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;

@FunctionalInterface
public interface UnaryOperationDelegate {
    JanitorObject perform(final JanitorScriptProcess process, final JanitorObject parameter) throws JanitorRuntimeException;
}
