package com.eischet.janitor.compiler.ast.expression;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.Ast;

/**
 * Expression.
 * Everything that can be evaluated to return a value is an expression.
 * It's the most basic building block of the language, together with statements.
 */
public interface Expression extends Ast {
    JanitorObject evaluate(final JanitorScriptProcess runningScript) throws JanitorRuntimeException;
}
