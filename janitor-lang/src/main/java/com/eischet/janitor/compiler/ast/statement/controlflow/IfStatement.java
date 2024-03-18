package com.eischet.janitor.compiler.ast.statement.controlflow;

import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.api.types.JanitorScriptProcess;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.statement.Statement;
import com.eischet.janitor.runtime.JanitorSemantics;

public class IfStatement extends Statement {
    private final Expression condition;
    private final Block block;
    private final Block elseBlock;

    public IfStatement(final Location location, final Expression condition, final Block block, final Block elseBlock) {
        super(location);
        this.condition = condition;
        this.block = block;
        this.elseBlock = elseBlock;
    }

    @Override
    public void execute(final JanitorScriptProcess runningScript) throws JanitorRuntimeException, JanitorControlFlowException {
        runningScript.setCurrentLocation(getLocation());
        final JanitorObject conditionValue = condition.evaluate(runningScript);
        if (conditionValue == null) {
            runningScript.trace(() -> "if condition evaluated to null: " + condition);
        }
        if (JanitorSemantics.isTruthy(conditionValue.janitorUnpack())) {
            block.execute(runningScript);
        } else if (elseBlock != null) {
            elseBlock.execute(runningScript);
        }
    }
}