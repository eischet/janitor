package com.eischet.janitor.compiler.ast.expression.binary;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.types.builtin.JBool;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.runtime.JanitorSemantics;
import com.eischet.janitor.compiler.ast.AstNode;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportableObject;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Logical AND of operands: and.
 * Deprecated but usable: &amp;&amp;.
 * This is slightly different from other binary operations in that it short-circuits.
 */
public class LogicAnd extends AstNode implements Expression, JsonExportableObject {
    private final Expression left;
    private final Expression right;

    /**
     * Constructor.
     * @param location where
     * @param left left operand
     * @param right right operand
     */
    public LogicAnd(final Location location, final Expression left, final Expression right) {
        super(location);
        this.left = left;
        this.right = right;
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess process) throws JanitorRuntimeException {
        process.setCurrentLocation(getLocation());
        final JanitorObject leftValue = left.evaluate(process).janitorUnpack();
        if (!JanitorSemantics.isTruthy(leftValue)) {
            return JBool.FALSE;
        }
        final JanitorObject rightValue = right.evaluate(process).janitorUnpack();
        return Janitor.toBool(JanitorSemantics.isTruthy(rightValue));
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .optional("left", left)
                .optional("right", right)
                .endObject();
    }

}
