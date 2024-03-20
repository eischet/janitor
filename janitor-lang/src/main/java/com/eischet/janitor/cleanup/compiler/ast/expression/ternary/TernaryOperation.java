package com.eischet.janitor.cleanup.compiler.ast.expression.ternary;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.api.api.types.JNull;
import com.eischet.janitor.cleanup.api.api.types.JanitorObject;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;
import com.eischet.janitor.cleanup.compiler.ast.AstNode;
import com.eischet.janitor.cleanup.compiler.ast.expression.Expression;
import com.eischet.janitor.cleanup.runtime.JanitorSemantics;

public abstract class TernaryOperation extends AstNode implements Expression {
    protected final Expression a;
    protected final Expression b;
    protected final Expression c;

    public TernaryOperation(final Location location, final Expression a, final Expression b, final Expression c) {
        super(location);
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public static class IfThenElse extends TernaryOperation {
        public IfThenElse(final Location location, final Expression a, final Expression b, final Expression c) {
            super(location, a, b, c);
        }

        @Override
        public JanitorObject evaluate(final JanitorScriptProcess runningScript) throws JanitorRuntimeException {
            runningScript.setCurrentLocation(getLocation());
            if (JanitorSemantics.isTruthy(a.evaluate(runningScript).janitorUnpack())) {
                return b.evaluate(runningScript).janitorUnpack();
            } else {
                if (c == null) {
                    return JNull.NULL;
                }
                return c.evaluate(runningScript).janitorUnpack();
            }
        }
    }

}
