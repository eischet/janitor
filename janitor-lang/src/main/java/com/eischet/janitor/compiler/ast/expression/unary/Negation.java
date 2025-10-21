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
 * Negation of an expression: -.
 * As in -17, not a-b, which is a binary subtraction operation.
 */
public class Negation extends AstNode implements Expression {
    private final Expression expr;

    /**
     * Constructor.
     *
     * @param location where
     * @param expr     what
     */
    public Negation(final Location location, final Expression expr) {
        super(location);
        this.expr = expr;
    }


    @Override
    public @NotNull JanitorObject evaluate(final JanitorScriptProcess process) throws JanitorRuntimeException {
        return JanitorSemantics.negate(process, expr.evaluate(process).janitorUnpack());
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .optional("expression", expr)
                .endObject();

    }

}
