package com.eischet.janitor.compiler.ast.statement.controlflow;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JIterable;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.statement.Statement;

import java.util.Iterator;

/**
 * For loop: for (i in iterable) { ... }.
 */
public class ForLoop extends Statement {
    private final String loopVar;
    private final Expression expression;
    private final Block block;

    /**
     * Constructor.
     * @param location where
     * @param loopVar loop variable
     * @param expression range expression
     * @param block loop body
     */
    public ForLoop(final Location location, final String loopVar, final Expression expression, final Block block) {
        super(location);
        this.loopVar = loopVar;
        this.expression = expression;
        this.block = block;
    }

    @Override
    public void execute(final JanitorScriptProcess process) throws JanitorRuntimeException, JanitorControlFlowException {
        try {
            process.setCurrentLocation(getLocation());
            final JanitorObject range = expression.evaluate(process).janitorUnpack();
            if (range instanceof JIterable iterableRange) {
                final Iterator<? extends JanitorObject> iterator = iterableRange.getIterator();
                while (iterator.hasNext()) {
                    try {
                        process.enterBlock(getLocation());
                        final JanitorObject next = iterator.next().janitorUnpack();
                        process.getCurrentScope().bind(process, loopVar, next);
                        try {
                            block.execute(process);
                        } catch (ContinueStatement.Continue ignored) {
                        }
                    } finally {
                        process.exitBlock();
                    }
                }
            } else {
                throw new JanitorArgumentException(process, "invalid range: " + range + "is not iterable");
            }
        } catch (BreakStatement.Break ignored) {
        }
    }

}
