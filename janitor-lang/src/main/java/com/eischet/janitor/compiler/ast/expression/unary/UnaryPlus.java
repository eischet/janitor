// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.compiler.ast.expression.unary;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.runtime.JanitorSemantics;
import com.eischet.janitor.compiler.ast.AstNode;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;
import org.jetbrains.annotations.NotNull;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Unary plus: +17, the counterpart of {@link Negation}. Like in Java and Python, and unlike JavaScript, it does not convert
 * values to numbers: it returns a number unchanged and rejects everything else.
 */
public class UnaryPlus extends AstNode implements Expression {
    private final Expression expr;

    /**
     * Constructor.
     *
     * @param location where
     * @param expr     what
     */
    public UnaryPlus(final Location location, final Expression expr) {
        super(location);
        this.expr = expr;
    }

    @Override
    public @NotNull JanitorObject evaluate(final JanitorScriptProcess process) throws JanitorRuntimeException {
        process.countInstruction();
        return JanitorSemantics.unaryPlus(process, expr.evaluate(process).janitorUnpack());
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .optional("expression", expr)
                .endObject();
    }

}
