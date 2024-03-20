package com.eischet.janitor.cleanup.compiler.ast.expression;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;
import com.eischet.janitor.cleanup.compiler.ast.Ast;

public interface Expression extends Ast {
    JanitorObject evaluate(final JanitorScriptProcess runningScript) throws JanitorRuntimeException;
}
