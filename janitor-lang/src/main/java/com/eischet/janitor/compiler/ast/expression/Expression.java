package com.eischet.janitor.compiler.ast.expression;

import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.JanitorScriptProcess;
import com.eischet.janitor.compiler.ast.Ast;

public interface Expression extends Ast {
    JanitorObject evaluate(final JanitorScriptProcess runningScript) throws JanitorRuntimeException;
}
