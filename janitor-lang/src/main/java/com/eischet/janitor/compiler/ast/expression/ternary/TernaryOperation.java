package com.eischet.janitor.compiler.ast.expression.ternary;

import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.compiler.ast.AstNode;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Ternary operations.
 * Right now, there's only one ternary operation: if-then-else.
 * BUT there are two syntaxes for it: if foo then bar else baz
 * and die equivalent foo ? bar : baz.
 */
public abstract class TernaryOperation extends AstNode implements Expression {
    protected final Expression a;
    protected final Expression b;
    protected final Expression c;

    /**
     * Constructor.
     * @param location where
     * @param a first operand
     * @param b second operand
     * @param c third operand
     */
    public TernaryOperation(final Location location, final Expression a, final Expression b, final Expression c) {
        super(location);
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .optional("a", a)
                .optional("b", b)
                .optional("c", c)
                .endObject();

    }


}
