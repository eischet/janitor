package com.eischet.janitor.compiler.ast.expression.ternary;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.builtin.JNull;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.runtime.JanitorSemantics;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * If-then-else expression.
 */
public class IfThenElse extends TernaryOperation {
    /**
     * Constructor.
     * @param location where
     * @param a first operand
     * @param b second operand
     * @param c third operand
     */
    public IfThenElse(final Location location, final Expression a, final Expression b, final Expression c) {
        super(location, a, b, c);
    }

    @Override
    public JanitorObject evaluate(final JanitorScriptProcess process) throws JanitorRuntimeException {
        process.setCurrentLocation(getLocation());
        if (JanitorSemantics.isTruthy(a.evaluate(process).janitorUnpack())) {
            return b.evaluate(process).janitorUnpack();
        } else {
            if (c == null) {
                return JNull.NULL;
            }
            return c.evaluate(process).janitorUnpack();
        }
    }

}
