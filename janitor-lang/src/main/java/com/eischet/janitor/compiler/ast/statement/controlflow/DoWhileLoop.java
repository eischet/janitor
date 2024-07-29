package com.eischet.janitor.compiler.ast.statement.controlflow;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.runtime.JanitorSemantics;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.statement.Statement;

/**
 * Do-while loop.
 */
public class DoWhileLoop extends Statement {

    private final Block block;
    private final Expression expression;

    /**
     * Constructor.
     * @param location where
     * @param block inner body of the loop
     * @param expression while expression (from the end of the loop)
     */
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
