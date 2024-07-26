package com.eischet.janitor.compiler.ast.statement.controlflow;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.builtin.JInt;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.statement.Statement;

/**
 * For-range loop: for (i from start to end) { ... }.
 */
public class ForRangeLoop extends Statement {
    private final String loopVar;
    private final Block block;
    private final Expression from;
    private final Expression to;
    //private final Expression step; // idea: stepping, e.g. for (i from 0 to 10 step 2) { ... }? Not sure if worth it.

    /**
     * Constructor.
     * @param location where
     * @param loopVar loop variable
     * @param from start of the range
     * @param to end of the range
     * @param block loop body
     */
    public ForRangeLoop(final Location location,
                        final String loopVar,
                        final Expression from,
                        final Expression to,
                        final Block block) {
        super(location);
        this.loopVar = loopVar;
        this.from = from;
        this.to = to;
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
                        runningScript.getCurrentScope().bind(runningScript, loopVar, runningScript.getEnvironment().getBuiltins().integer(i));
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
