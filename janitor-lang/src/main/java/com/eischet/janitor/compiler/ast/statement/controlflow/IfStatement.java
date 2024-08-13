package com.eischet.janitor.compiler.ast.statement.controlflow;

import com.eischet.janitor.api.JanitorScriptProcess;
import com.eischet.janitor.api.errors.runtime.JanitorControlFlowException;
import com.eischet.janitor.api.errors.runtime.JanitorRuntimeException;
import com.eischet.janitor.api.scopes.Location;
import com.eischet.janitor.api.types.JanitorObject;
import com.eischet.janitor.runtime.JanitorSemantics;
import com.eischet.janitor.compiler.ast.expression.Expression;
import com.eischet.janitor.compiler.ast.statement.Statement;
import com.eischet.janitor.toolbox.json.api.JsonException;
import com.eischet.janitor.toolbox.json.api.JsonExportableObject;
import com.eischet.janitor.toolbox.json.api.JsonOutputStream;

import static com.eischet.janitor.api.util.ObjectUtilities.simpleClassNameOf;

/**
 * If statement: if (condition) { ... } else { ... }.
 */
public class IfStatement extends Statement implements JsonExportableObject {
    private final Expression condition;
    private final Block block;
    private final Block elseBlock;

    /**
     * Constructor.
     * @param location where
     * @param condition if condition
     * @param block then-block
     * @param elseBlock else-block (optional)
     */
    public IfStatement(final Location location, final Expression condition, final Block block, final Block elseBlock) {
        super(location);
        this.condition = condition;
        this.block = block;
        this.elseBlock = elseBlock;
    }

    @Override
    public void execute(final JanitorScriptProcess process) throws JanitorRuntimeException, JanitorControlFlowException {
        process.setCurrentLocation(getLocation());
        final JanitorObject conditionValue = condition.evaluate(process);
        if (conditionValue == null) {
            process.trace(() -> "if condition evaluated to null: " + condition);
        }
        if (JanitorSemantics.isTruthy(conditionValue.janitorUnpack())) {
            block.execute(process);
        } else if (elseBlock != null) {
            elseBlock.execute(process);
        }
    }

    @Override
    public void writeJson(JsonOutputStream producer) throws JsonException {
        producer.beginObject()
                .optional("type", simpleClassNameOf(this))
                .optional("condition", condition)
                .optional("block", block)
                .optional("elseBlock", elseBlock)
                .endObject();
    }
}
