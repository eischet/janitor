package com.eischet.janitor.compiler.ast.statement.controlflow;

import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorScriptProcess;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.statement.Statement;
import com.eischet.janitor.runtime.JanitorSemantics;

public class WhileLoop extends Statement {
    private final Expression expression;
    private final Block block;

    public WhileLoop(final Location location, final Expression expression, final Block block) {
        super(location);
        this.expression = expression;
        this.block = block;
    }

    @Override
    public void execute(final JanitorScriptProcess runningScript) throws JanitorRuntimeException, JanitorControlFlowException {
        try {
            runningScript.setCurrentLocation(getLocation());
            while (JanitorSemantics.isTruthy(expression.evaluate(runningScript).janitorUnpack())) {
                try {
                    block.execute(runningScript);
                } catch (ContinueStatement.Continue ignored) {
                }
            }
        } catch (BreakStatement.Break ignored) {
        }
    }

}
