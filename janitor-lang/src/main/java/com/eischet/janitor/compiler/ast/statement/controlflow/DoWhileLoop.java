package com.eischet.janitor.compiler.ast.statement.controlflow;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.glue.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.runtime.JanitorSemantics;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.statement.Statement;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportableObject;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * Do-while loop.
 */
public class DoWhileLoop extends Statement implements JsonExportableObject {

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
    public void execute(final JanitorScriptProcess process) throws JanitorRuntimeException, JanitorControlFlowException {
        try {
            process.setCurrentLocation(getLocation());
            do {
                try {
                    block.execute(process);
                } catch (ContinueStatement.Continue ignored) {
                }
            } while (JanitorSemantics.isTruthy(expression.evaluate(process).janitorUnpack()));
        } catch (BreakStatement.Break ignored) {
        }
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .optional("block", block)
                .optional("expression", expression)
                .endObject();
    }

}
