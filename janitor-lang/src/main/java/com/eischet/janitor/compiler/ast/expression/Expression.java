// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.compiler.ast.expression;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorError;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.Ast;
import com.eischet.janitor.toolbox.json.api.JsonExportableObject;
import org.jetbrains.annotations.NotNull;

/**
 * Expression.
 * Everything that can be evaluated to return a value is an expression.
 * It's the most basic building block of the language, together with statements.
 */
public interface Expression extends Ast, JsonExportableObject {
    @NotNull JanitorObject evaluate(final JanitorScriptProcess process) throws JanitorRuntimeException, JanitorError;

    /**
     * Evaluates this expression as the target of an assignment, e.g. the left side of "foo.bar = baz".
     * Expressions that look up properties return them without reading their current values.
     * @param process the running script
     * @return the result, which should usually be assignable
     * @throws JanitorRuntimeException on errors
     * @throws JanitorError on errors
     */
    default @NotNull JanitorObject evaluateForAssignment(final JanitorScriptProcess process) throws JanitorRuntimeException, JanitorError {
        return evaluate(process);
    }
}
