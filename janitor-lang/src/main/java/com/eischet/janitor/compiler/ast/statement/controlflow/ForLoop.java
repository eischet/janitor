package com.eischet.janitor.compiler.ast.statement.controlflow;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.traits.JIterable;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.statement.Statement;

import java.util.Iterator;

public class ForLoop extends Statement {
    private final String loopVar;
    private final Expression expression;
    private final Block block;

    public ForLoop(final Location location, final String loopVar, final Expression expression, final Block block) {
        super(location);
        this.loopVar = loopVar;
        this.expression = expression;
        this.block = block;
    }

    @Override
    public void execute(final JanitorScriptProcess runningScript) throws JanitorRuntimeException, JanitorControlFlowException {
        try {
            runningScript.setCurrentLocation(getLocation());
            final JanitorObject range = expression.evaluate(runningScript).janitorUnpack();
            if (range instanceof JIterable iterableRange) {
                final Iterator<? extends JanitorObject> iterator = iterableRange.getIterator();
                while (iterator.hasNext()) {
                    try {
                        runningScript.enterBlock(getLocation());
                        final JanitorObject next = iterator.next().janitorUnpack();
                        runningScript.getCurrentScope().bind(runningScript, loopVar, next);
                        try {
                            block.execute(runningScript);
                        } catch (ContinueStatement.Continue ignored) {
                        }
                    } finally {
                        runningScript.exitBlock();
                    }
                }
            } else {
                throw new JanitorArgumentException(runningScript, "invalid range: " + range + "is not iterable");
            }
        } catch (BreakStatement.Break ignored) {
        }
    }

}
