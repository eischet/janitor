package com.eischet.janitor.compiler.ast.statement.controlflow;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JInt;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.statement.Statement;

public class ForRangeLoop extends Statement {
    private final String loopVar;
    private final Block block;
    private final Expression from;
    private final Expression to;
    //private final Expression step;

    public ForRangeLoop(final Location location,
                        final String loopVar,
                        final Expression from,
                        final Expression to, /* LATER: final Expression step, */
                        final Block block) {
        super(location);
        this.loopVar = loopVar;
        this.from = from;
        this.to = to;
        // LATER: this.step = step;
        this.block = block;
    }

    @Override
    public void execute(final JanitorScriptProcess runningScript) throws JanitorRuntimeException, JanitorControlFlowException {
        try {
            runningScript.setCurrentLocation(getLocation());
            final JanitorObject start = from.evaluate(runningScript).janitorUnpack();
            final JanitorObject end = to.evaluate(runningScript).janitorUnpack();
            if (start instanceof JInt startInt && end instanceof JInt endInt) {
                final long endIntValue = endInt.getValue();
                for (long i = startInt.getValue(); i <= endIntValue; i++) {
                    try {
                        runningScript.enterBlock(getLocation());
                        runningScript.getCurrentScope().bind(runningScript, loopVar, new JInt(i));
                        try {
                            block.execute(runningScript);
                        } catch (ContinueStatement.Continue ignored) {
                        }
                    } finally {
                        runningScript.exitBlock();
                    }
                }
            } else {
                throw new JanitorArgumentException(runningScript, "invalid range: from " + start + " to " + end + ", expecting integer values for the range!");
            }
        } catch (BreakStatement.Break ignored) {
        }
    }

}
