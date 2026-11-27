// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.compiler.ast.statement.controlflow;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorArgumentException;
import com.eischet.janitor.api.errors.glue.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JIterable;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.statement.Statement;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportableObject;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;

import java.util.Iterator;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * For loop: for (i in iterable) { ... }, optionally followed by else { ... }, which runs when the loop body did not run at all.
 */
public class ForLoop extends Statement implements JsonExportableObject {
    private final String loopVar;
    private final Expression expression;
    private final Block block;
    private final Block elseBlock;

    /**
     * Constructor.
     * @param location where
     * @param loopVar loop variable
     * @param expression range expression
     * @param block loop body
     * @param elseBlock block to run when there was no iteration at all (optional)
     */
    public ForLoop(final Location location, final String loopVar, final Expression expression, final Block block, final Block elseBlock) {
        super(location);
        this.loopVar = loopVar;
        this.expression = expression;
        this.block = block;
        this.elseBlock = elseBlock;
    }

    @Override
    public void execute(final JanitorScriptProcess process) throws JanitorRuntimeException, JanitorControlFlowException {
        boolean iterated = false;
        try {
            process.setCurrentLocation(getLocation());
            final JanitorObject range = expression.evaluate(process).janitorUnpack();
            if (range instanceof JIterable iterableRange) {
                final Iterator<? extends JanitorObject> iterator = iterableRange.getIterator();
                while (iterator.hasNext()) {
                    iterated = true;
                    try {
                        process.enterBlock(getLocation());
                        process.countInstruction();
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
        if (!iterated && elseBlock != null) {
            elseBlock.execute(process);
        }
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .optional("loopVar", loopVar)
                .optional("expression", expression)
                .optional("block", block)
                .optional("elseBlock", elseBlock)
                .endObject();
    }

}
