package com.eischet.janitor.cleanup.compiler.ast.statement.controlflow;

import com.eischet.janitor.cleanup.api.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.cleanup.api.api.scopes.Location;
import com.eischet.janitor.cleanup.api.api.types.JanitorScriptProcess;
import com.eischet.janitor.cleanup.compiler.ast.expression.Expression;
import com.eischet.janitor.cleanup.compiler.ast.statement.Statement;
import com.eischet.janitor.cleanup.runtime.JanitorSemantics;

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
