package com.eischet.janitor.cleanup.compiler.ast.statement.controlflow;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;
import com.eischet.janitor.cleanup.compiler.ast.expression.Expression;
import com.eischet.janitor.cleanup.compiler.ast.statement.Statement;
import com.eischet.janitor.cleanup.runtime.JanitorSemantics;

public class DoWhileLoop extends Statement {

    private final Block block;
    private final Expression expression;

    public DoWhileLoop(final Location location, final Block block, final Expression expression) {
        super(location);
        this.block = block;
        this.expression = expression;
    }

    @Override
    public void execute(final JanitorScriptProcess runningScript) throws JanitorRuntimeException, JanitorControlFlowException {
        try {
            runningScript.setCurrentLocation(getLocation());
            do {
                try {
                    block.execute(runningScript);
                } catch (ContinueStatement.Continue ignored) {
                }
            } while (JanitorSemantics.isTruthy(expression.evaluate(runningScript).janitorUnpack()));
        } catch (BreakStatement.Break ignored) {
        }
    }
}
