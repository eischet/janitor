package com.eischet.janitor.compiler.ast.statement.controlflow;

import com.eischet.janitor.api.Janitor;
import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.glue.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.builtin.JInt;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.statement.Statement;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportableObject;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * For-range loop: for (i from start to end) { ... }.
 */
public class ForRangeLoop extends Statement implements JsonExportableObject {
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
    public void execute(final JanitorScriptProcess process) throws JanitorRuntimeException, JanitorControlFlowException {
        try {
            process.setCurrentLocation(getLocation());
            final JanitorObject start = from.evaluate(process).janitorUnpack();
            final JanitorObject end = to.evaluate(process).janitorUnpack();
            if (start instanceof JInt startInt && end instanceof JInt endInt) {
                final long endIntValue = endInt.getValue();
                for (long i = startInt.getValue(); i <= endIntValue; i++) {
                    try {
                        process.enterBlock(getLocation());
                        process.getCurrentScope().bind(process, loopVar, Janitor.integer(i));
                        try {
                            block.execute(process);
                        } catch (ContinueStatement.Continue ignored) {
                        }
                    } finally {
                        process.exitBlock();
                    }
                }
            } else {
                throw new JanitorArgumentException(process, "invalid range: from " + start + " to " + end + ", expecting integer values for the range!");
            }
        } catch (BreakStatement.Break ignored) {
        }
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .optional("var", loopVar)
                .optional("from", from)
                .optional("to", to)
                .optional("block", block)
                .endObject();
    }

}
