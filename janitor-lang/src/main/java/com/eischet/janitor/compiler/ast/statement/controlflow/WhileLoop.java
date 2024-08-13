package com.eischet.janitor.compiler.ast.statement.controlflow;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
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
 * While loop: while (condition) { ... }.
 */
public class WhileLoop extends Statement implements JsonExportableObject {
    private final Expression expression;
    private final Block block;

    /**
     * Constructor.
     * @param location where
     * @param expression condition
     * @param block loop body
     */
    public WhileLoop(final Location location, final Expression expression, final Block block) {
        super(location);
        this.expression = expression;
        this.block = block;
    }

    @Override
    public void execute(final JanitorScriptProcess process) throws JanitorRuntimeException, JanitorControlFlowException {
        try {
            process.setCurrentLocation(getLocation());
            while (JanitorSemantics.isTruthy(expression.evaluate(process).janitorUnpack())) {
                try {
                    block.execute(process);
                } catch (ContinueStatement.Continue ignored) {
                }
            }
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
