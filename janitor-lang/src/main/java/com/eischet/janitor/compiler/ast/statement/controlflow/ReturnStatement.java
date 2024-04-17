package com.eischet.janitor.compiler.ast.statement.controlflow;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JNull;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.statement.Statement;

public class ReturnStatement extends Statement {

    public static class Return extends JanitorControlFlowException {
        private final JanitorObject value;

        // LATER: ich glaube nicht, dass das eine gute Idee ist, aber ich weiß sonst nicht wohin damit im Moment.
        public Return(final JanitorObject value) {
            this.value = value;
        }

        public JanitorObject getValue() {
            return value.janitorUnpack();
        }
    }

    protected static final Return RETURN_NOTHING = new Return(JNull.NULL);

    private final Expression expression;

    public ReturnStatement(final Location location, final Expression expression) {
        super(location);
        this.expression = expression;
    }

    @Override
    public void execute(final JanitorScriptProcess runningScript) throws JanitorRuntimeException, Return {
        runningScript.setCurrentLocation(getLocation());
        if (expression != null) {
            final JanitorObject returnValue = expression.evaluate(runningScript).janitorUnpack();
            runningScript.trace(() -> "return value: " + returnValue);
            throw new Return(returnValue);
        } else {
            throw RETURN_NOTHING;
        }
    }

}
