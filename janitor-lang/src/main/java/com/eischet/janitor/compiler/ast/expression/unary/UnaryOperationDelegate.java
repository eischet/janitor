package com.eischet.janitor.compiler.ast.expression.unary;

import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.JanitorScriptProcess;

@FunctionalInterface
public interface UnaryOperationDelegate {
    JanitorObject perform(final JanitorScriptProcess process, final JanitorObject parameter) throws JanitorRuntimeException;
}
