package com.eischet.janitor.compiler.ast.statement.controlflow;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.util.JanitorSemantics;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.statement.Statement;

public class WhileStatement extends Statement {
    private final Expression condition;
    private final Block block;

    public WhileStatement(final Location location, final Expression condition, final Block block) {
        super(location);
        this.condition = condition;
        this.block = block;
    }

    @Override
    public void execute(final JanitorScriptProcess runningScript) throws JanitorRuntimeException, JanitorControlFlowException {
        while (JanitorSemantics.isTruthy(condition.evaluate(runningScript).janitorUnpack())) {
            runningScript.setCurrentLocation(getLocation());
            block.execute(runningScript);
        }
    }
}
